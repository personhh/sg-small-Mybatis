package com.cps.mybatis.executor;

import com.cps.mybatis.cache.CacheKey;
import com.cps.mybatis.cache.impl.PerpetualCache;
import com.cps.mybatis.mapping.BoundSql;
import com.cps.mybatis.mapping.MappedStatement;
import com.cps.mybatis.mapping.ParameterMapping;
import com.cps.mybatis.reflection.MetaObject;
import com.cps.mybatis.session.Configuration;
import com.cps.mybatis.session.LocalCacheScope;
import com.cps.mybatis.session.ResultHandler;
import com.cps.mybatis.session.defaults.RowBounds;
import com.cps.mybatis.transaction.Transaction;
import com.cps.mybatis.type.TypeHandlerRegistry;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author cps
 * @description: 执行器抽象基类
 * @date 2024/1/19 13:37
 * @OtherDescription: 在抽象基类中封装了执行器的全部接口，这样具体的子类继承抽象类后，
 * 就不用在处理这些共性的方法。与此同时在 query 查询方法中，封装一些必要的流程处理，如果检测关闭等，
 * 在 Mybatis 源码中还有一些缓存的操作，这里暂时剔除掉，以核心流程为主。
 */
public abstract class BaseExecutor implements Executor{
    private org.slf4j.Logger logger = LoggerFactory.getLogger(BaseExecutor.class);

    protected Configuration configuration;
    protected Transaction transaction;
    protected Executor wrapper;

    private boolean closed;
    // 本地缓存
    protected PerpetualCache localCache;
    // 查询堆栈
    protected int queryStack = 0;


    public BaseExecutor(Configuration configuration, Transaction transaction) {
        this.configuration = configuration;
        this.transaction = transaction;
        this.wrapper = this;
        this.localCache = new PerpetualCache("LocalCache");
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, ResultHandler resultHandler,RowBounds rowBounds) throws SQLException {
        //1.获得绑定sql语句
        BoundSql boundSql = ms.getBoundSql(parameter);
        //2.创建缓存key
        CacheKey key = createCacheKey(ms,parameter,rowBounds,boundSql);
        return query(ms, parameter, resultHandler, rowBounds, key, boundSql);
    }


    //查询数据缓存
    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, ResultHandler resultHandler, RowBounds rowBounds,CacheKey key ,BoundSql boundSql) throws SQLException{
        if(closed){
            throw new RuntimeException("Executor was closed");
        }// 清理局部缓存，查询堆栈为0则清理。queryStack 避免递归调用清理
        if (queryStack == 0 && ms.isFlushCacheRequired()) {
            clearLocalCache();
        }
        List<E> list;
        try {
            queryStack++;
            // 根据cacheKey从localCache中查询数据
            list = resultHandler == null ? (List<E>) localCache.getObject(key) : null;
            if (list == null) {
                list = queryFromDatabase(ms, parameter, rowBounds, resultHandler, key, boundSql);
            }
        } finally {
            queryStack--;
        }
        if (queryStack == 0) {
            if (configuration.getLocalCacheScope() == LocalCacheScope.STATEMENT) {
                clearLocalCache();
            }
        }
        return list;

    }

    //存放缓存数据
    private <E> List<E> queryFromDatabase(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
        List<E> list;
        localCache.putObject(key, ExecutionPlaceholder.EXECUTION_PLACEHOLDER);
        try {
            list = doQuery(ms, parameter, resultHandler, rowBounds, boundSql);
        } finally {
            localCache.removeObject(key);
        }
        // 存入缓存
        localCache.putObject(key, list);
        return list;
    }


    protected abstract  <E> List<E> doQuery(MappedStatement ms, Object parameter, ResultHandler resultHandler, RowBounds rowBounds, BoundSql boundSql);

    @Override
    public int update(MappedStatement ms, Object parameter) throws Exception {
        if (closed) {
            throw new RuntimeException("Executor was closed.");
        }
        clearLocalCache();
        return doUpdate(ms, parameter);

    }

    protected abstract int doUpdate(MappedStatement ms, Object parameter) throws Exception;

    @Override
    public void commit(boolean required) throws SQLException {
        if(closed){
            throw new RuntimeException("Cannot commit, transaction is already closed");
        }
        clearLocalCache();
        if(required){
            transaction.commit();
        }
    }

    @Override
    public Transaction getTransaction() {
        if(closed){
            throw new RuntimeException("Executor was closed.");
        }
        return transaction;
    }

    @Override
    public void rollback(boolean required) throws SQLException {
        if(!closed){
            try{
                clearLocalCache();
            }finally {
                if(required){
                    transaction.rollback();
                }
            }
        }
    }

    @Override
    public void close(boolean forceRollback) {
        try{
            try {
                rollback(forceRollback);
            }
            finally {
                transaction.close();
            }
        }catch (Exception e){
            logger.warn("Unexpected exception on closing transaction. Cause: " + e);
        }finally {
            transaction = null;
            closed = true;
        }
    }

    protected void closeStatement(Statement statement){
        if(statement != null){
            try{
                statement.close();
            }catch (SQLException ignore){

            }
        }
    }

    @Override
    public void clearLocalCache(){
        if(!closed){
            localCache.clear();
        }
    }

    //创建缓存key
    @Override
    public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
        if (closed) {
            throw new RuntimeException("Executor was closed.");
        }
        CacheKey cacheKey = new CacheKey();
        cacheKey.update(ms.getId());
        cacheKey.update(rowBounds.getOffset());
        cacheKey.update(rowBounds.getLimit());
        cacheKey.update(boundSql.getSql());
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        TypeHandlerRegistry typeHandlerRegistry = ms.getConfiguration().getTypeHandlerRegistry();
        for (ParameterMapping parameterMapping : parameterMappings) {
            Object value;
            String propertyName = parameterMapping.getProperty();
            if (boundSql.hasAdditionalParameter(propertyName)) {
                value = boundSql.getAdditionalParameter(propertyName);
            } else if (parameterObject == null) {
                value = null;
            } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                value = parameterObject;
            } else {
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                value = metaObject.getValue(propertyName);
            }
            cacheKey.update(value);
        }
        if (configuration.getEnvironment() != null) {
            cacheKey.update(configuration.getEnvironment().getId());
        }
        return cacheKey;
    }

}
