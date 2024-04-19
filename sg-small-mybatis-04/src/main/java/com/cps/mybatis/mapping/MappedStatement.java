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
    /**sql语句（处理之后）*/
    private BoundSql boundSql;


    MappedStatement(){

    }

    public static class Builder{
        private MappedStatement mappedStatement = new MappedStatement();

        public Builder(Configuration configuration, String id, SqlCommandType sqlCommandType, BoundSql boundSql) {
            mappedStatement.configuration = configuration;
            mappedStatement.id = id;
            mappedStatement.sqlCommandType = sqlCommandType;
            mappedStatement.boundSql = boundSql;
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

    public void setId(String id) {
        this.id = id;
    }

    public SqlCommandType getSqlCommandType() {
        return sqlCommandType;
    }

    public void setSqlCommandType(SqlCommandType sqlCommandType) {
        this.sqlCommandType = sqlCommandType;
    }

    public BoundSql getBoundSql(){
        return boundSql;
    }
}
