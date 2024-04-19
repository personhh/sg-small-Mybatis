package com.cps.mybatis.executor.statement;

import com.cps.mybatis.executor.Executor;
import com.cps.mybatis.mapping.BoundSql;
import com.cps.mybatis.mapping.MappedStatement;
import com.cps.mybatis.session.ResultHandler;
import com.cps.mybatis.session.defaults.RowBounds;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author cps
 * @description: 预处理语句处理器
 * @date 2024/1/19 16:11
 * @OtherDescription: 在预处理语句处理器中包括 instantiateStatement 预处理 SQL、parameterize 设置参数，以及 query 查询的执行的操作。
 */
public class PreparedStatementHandler extends BaseStatementHandler{
    public PreparedStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, ResultHandler resultHandler, RowBounds rowBounds, BoundSql boundSql) {
        super(executor,mappedStatement,parameterObject,resultHandler, rowBounds, boundSql);
    }


    @Override
    protected Statement instantiateStatement(Connection connection) throws SQLException {
        String sql = boundSql.getSql();
        return connection.prepareStatement(sql);
    }

    //将参数填入这个语句
    @Override
    public void parameterize(Statement statement) throws SQLException {
        parameterHandler.setParameters((PreparedStatement) statement);
    }

    @Override
    public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
        PreparedStatement ps = (PreparedStatement) statement;//获得sql语句
        ps.execute();//调用执行方法进行查询
        return resultSetHandler.<E>handleResultSets(ps);//返回结果集
    }
}
