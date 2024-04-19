package com.cps.mybatis.mapping;

import com.cps.mybatis.session.Configuration;

import java.util.Map;

//映射的语句类
public class MappedStatement {

    /**配置类*/
    private Configuration configuration;
    /**语句id*/
    private String id;
    /**指令类型：未知、增删改查*/
    private SqlCommandType sqlCommandType;
    /**输入参数类型*/
    private String parameterType;
    /**输出参数类型*/
    private String resultType;
    /**sql语句*/
    private String sql;
    /**参数*/
    private Map<Integer,String> parameter;

    MappedStatement(){

    }

    public static class Builder{
        private MappedStatement mappedStatement = new MappedStatement();

        public Builder(Configuration configuration, String id, SqlCommandType sqlCommandType, String parameterType, String resultType, String sql, Map<Integer, String> parameter) {
            mappedStatement.configuration = configuration;
            mappedStatement.id = id;
            mappedStatement.sqlCommandType = sqlCommandType;
            mappedStatement.parameterType = parameterType;
            mappedStatement.resultType = resultType;
            mappedStatement.sql = sql;
            mappedStatement.parameter = parameter;
        }

        public MappedStatement build(){
            assert mappedStatement.configuration != null;
            assert mappedStatement.id != null;
            return mappedStatement;
        }
    }

    public Configuration getConfiguration(){
        return configuration;
    }

    public void setConfiguration(Configuration configuration){
        this.configuration = configuration;
    }

    public String getId() {
        return id;
    }

    public SqlCommandType getSqlCommandType() {
        return sqlCommandType;
    }

    public String getSql() {
        return sql;
    }

}
