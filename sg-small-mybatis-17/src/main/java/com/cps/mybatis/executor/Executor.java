package com.cps.mybatis.executor;

import com.cps.mybatis.cache.CacheKey;
import com.cps.mybatis.mapping.BoundSql;
import com.cps.mybatis.mapping.MappedStatement;
import com.cps.mybatis.session.ResultHandler;
import com.cps.mybatis.session.defaults.RowBounds;
import com.cps.mybatis.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;

/**
 * @author cps
 * @description: 执行器接口类
 * @date 2024/1/19 13:25
 * @OtherDescription: 在执行器中定义的接口包括事务相关的处理方法和执行SQL查询的操作，随着后续功能的迭代还会继续补充其他的方法。
 */
public interface Executor {
    ResultHandler NO_RESULT_HANDLER = null;

    /**
     * 执行SQL查询语句的方法(含缓存）
     * ms：语句映射器
     * parameter:参数
     * resultHandler:结果处理器
     * boundSql:sql语句
     * */
    <E> List<E> query(MappedStatement ms, Object parameter, ResultHandler resultHandler, RowBounds rowBounds, CacheKey cacheKey, BoundSql boundSql) throws SQLException;

    <E> List<E> query(MappedStatement ms, Object parameter, ResultHandler resultHandler,RowBounds rowBounds ) throws SQLException;

    //执行增删改sql
    int update(MappedStatement ms, Object parameter) throws Exception;

    /**获取事务*/
    Transaction getTransaction();

    /**提交事务 */
    void commit(boolean required) throws SQLException;
    /**回滚事务 */
    void rollback(boolean required) throws SQLException;
    /**关闭事务 */
    void close(boolean forceRollback);

    // 清理Session缓存
    void clearLocalCache();

    // 创建缓存 Key
    CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql);

}
