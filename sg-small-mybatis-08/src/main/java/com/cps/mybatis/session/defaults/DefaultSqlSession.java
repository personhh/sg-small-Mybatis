package com.cps.mybatis.session.defaults;

import com.cps.mybatis.executor.Executor;
import com.cps.mybatis.mapping.MappedStatement;
import com.cps.mybatis.session.Configuration;
import com.cps.mybatis.session.SqlSession;

import java.util.List;

public class DefaultSqlSession implements SqlSession {

    /**
     * 映射器注册机
     */
    private Configuration configuration;

    /**
     * sql执行器*/
    private Executor executor;

    public DefaultSqlSession(Configuration configuration , Executor executor) {
        this.configuration = configuration;
        this.executor = executor;
    }

    @Override
    public <T> T selectOne(String statement) {
        return (T) ("你被代理了！" + statement);
    }

    //查询一个
    @Override
    public <T> T selectOne(String statement, Object parameter) {
        MappedStatement mappedStatement = configuration.getMappedStatement(statement);
        List<T> list = executor.query(mappedStatement, parameter, Executor.NO_RESULT_HANDLER, mappedStatement.getSqlSource().getBoundSql(parameter));
        return list.get(0);
    }

    @Override
    public <T> T getMapper(Class<T> type) {
        return configuration.getMapper(type,this);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }
}
