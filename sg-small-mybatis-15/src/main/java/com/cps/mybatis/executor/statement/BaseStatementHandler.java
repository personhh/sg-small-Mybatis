package com.cps.mybatis.executor.statement;

import com.cps.mybatis.executor.Executor;
import com.cps.mybatis.executor.keygen.KeyGenerator;
import com.cps.mybatis.executor.parameter.ParameterHandler;
import com.cps.mybatis.executor.resultset.ResultSetHandler;
import com.cps.mybatis.mapping.BoundSql;
import com.cps.mybatis.mapping.MappedStatement;
import com.cps.mybatis.session.Configuration;
import com.cps.mybatis.session.ResultHandler;
import com.cps.mybatis.session.defaults.RowBounds;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author cps
 * @description: 语句处理器的抽象基类
 * @date 2024/1/19 14:21
 * @OtherDescription: Other things
 */
public abstract class BaseStatementHandler implements StatementHandler{

    protected final Configuration configuration;
    protected final Executor executor;
    protected final MappedStatement mappedStatement;

    protected final Object parameterObject;
    protected final ResultSetHandler resultSetHandler;
    protected final ParameterHandler parameterHandler;
    protected BoundSql boundSql;
    protected final RowBounds rowBounds;


    public BaseStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, ResultHandler resultHandler, RowBounds rowBounds,  BoundSql boundSql) {
        this.configuration = mappedStatement.getConfiguration();
        this.executor = executor;
        this.mappedStatement = mappedStatement;
        this.rowBounds = rowBounds;

        if(boundSql == null){
            boundSql = mappedStatement.getBoundSql(parameterObject);
        }

        this.boundSql = boundSql;
        //参数和结果集
        this.parameterObject = parameterObject;
        this.parameterHandler = configuration.newParameterHandler(mappedStatement,parameterObject,boundSql);
        this.resultSetHandler = configuration.newResultSetHandler(executor,mappedStatement,resultHandler,rowBounds, boundSql);
    }


    /**
     * 准备语句
     */

    @Override
    public Statement prepare(Connection connection) throws SQLException {
        Statement statement = null;
        try{
            //实例化 Statement
             statement = instantiateStatement(connection);
             //参数设置，可以被抽取，提供配置
            statement.setQueryTimeout(350);//设置查询超时时间
            statement.setFetchDirection(1000);//为驱动程序提供一个提示，说明在使用此Statement对象创建的ResultSet对象中处理行的方向。
            // 默认值为ResultSet.FETCH_FORWARD。
            // 请注意，此方法为此Statement对象生成的结果集设置默认获取方向。
            // 每个结果集都有自己的方法来获取和设置自己的获取方向。
            return  statement;
        }catch (Exception e){
            throw new RuntimeException("Error preparing statement. Cause: " + e, e);
        }
    }

    /**
     * 初始化语句*/
    protected abstract Statement instantiateStatement(Connection connection) throws SQLException;

    protected void generateKeys(Object parameter){
        KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
        keyGenerator.processBefore(executor,mappedStatement,null, parameter);
    }
}
