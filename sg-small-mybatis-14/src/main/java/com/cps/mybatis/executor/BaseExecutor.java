package com.cps.mybatis.executor;

import com.cps.mybatis.mapping.BoundSql;
import com.cps.mybatis.mapping.MappedStatement;
import com.cps.mybatis.session.Configuration;
import com.cps.mybatis.session.ResultHandler;
import com.cps.mybatis.session.defaults.RowBounds;
import com.cps.mybatis.transaction.Transaction;
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


    public BaseExecutor(Configuration configuration, Transaction transaction) {
        this.configuration = configuration;
        this.transaction = transaction;
        this.wrapper = this;
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, ResultHandler resultHandler, RowBounds rowBounds) throws SQLException {
        BoundSql boundSql = ms.getBoundSql(parameter);
        return query(ms, parameter, resultHandler, rowBounds, boundSql);
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, ResultHandler resultHandler, RowBounds rowBounds, BoundSql boundSql) {
        if(closed){
            throw new RuntimeException("Executor was closed");
        }
        return doQuery(ms,parameter,resultHandler, rowBounds, boundSql);
    }

    protected abstract  <E> List<E> doQuery(MappedStatement ms, Object parameter, ResultHandler resultHandler, RowBounds rowBounds, BoundSql boundSql);

    @Override
    public int update(MappedStatement ms, Object parameter) throws Exception {
        return doUpdate(ms, parameter);
    }

    protected abstract int doUpdate(MappedStatement ms, Object parameter) throws Exception;

    @Override
    public void commit(boolean required) throws SQLException {
        if(closed){
            throw new RuntimeException("Cannot commit, transaction is already closed");
        }
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
           if(required){
               transaction.rollback();
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
}
