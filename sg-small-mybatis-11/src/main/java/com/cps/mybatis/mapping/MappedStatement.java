package com.cps.mybatis.mapping;

import com.cps.mybatis.scripting.LanguageDriver;
import com.cps.mybatis.session.Configuration;

import java.util.List;

//映射的语句类
public class MappedStatement {

    /**配置类*/
    private Configuration configuration;
    /**语句id*/
    private String id;
    /**指令类型：未知、增删改查*/
    private SqlCommandType sqlCommandType;
    /**sql源码*/
    private SqlSource sqlSource;
    /**返回类型*/
    Class<?> resultType;

    private LanguageDriver lang;

    private List<ResultMap> resultMaps ;
    MappedStatement(){

    }

    public BoundSql getBoundSql(Object parameterObject){
        return sqlSource.getBoundSql(parameterObject);
    }

    public static class Builder{
        private MappedStatement mappedStatement = new MappedStatement();

        public Builder(Configuration configuration, String id, SqlCommandType sqlCommandType, SqlSource sqlSource, Class<?> resultType) {
            mappedStatement.configuration = configuration;
            mappedStatement.id = id;
            mappedStatement.sqlCommandType = sqlCommandType;
            mappedStatement.sqlSource = sqlSource;
            mappedStatement.resultType = resultType;
            mappedStatement.lang = configuration.getDefaultScriptingLanguageInstance();
        }

        public MappedStatement build(){
            assert mappedStatement.configuration != null;
            assert mappedStatement.id != null;
            return mappedStatement;
        }

        public String id(){
            return mappedStatement.id;
        }

        public Builder resultMaps(List<ResultMap> resultMaps){
            mappedStatement.resultMaps = resultMaps;
            return this;
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

    public SqlSource getSqlSource() {
        return sqlSource;
    }

    public LanguageDriver getLang(){
        return lang;
    }

    public List<ResultMap> getResultMaps() {
        return resultMaps;
    }


}
