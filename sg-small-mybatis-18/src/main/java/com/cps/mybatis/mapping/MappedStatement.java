package com.cps.mybatis.mapping;

import com.cps.mybatis.cache.Cache;
import com.cps.mybatis.executor.keygen.Jdbc3KeyGenerator;
import com.cps.mybatis.executor.keygen.KeyGenerator;
import com.cps.mybatis.executor.keygen.NoKeyGenerator;
import com.cps.mybatis.scripting.LanguageDriver;
import com.cps.mybatis.session.Configuration;

import java.util.Collections;
import java.util.List;

//映射的语句类
public class MappedStatement {

    private String resource;
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

    private KeyGenerator keyGenerator;
    //属性
    private String[] keyProperties;
    //别名
    private String[] keyColumns;

    private boolean flushCacheRequired;
    private Cache cache;
    private boolean useCache;
    MappedStatement(){

    }


    public BoundSql getBoundSql(Object parameterObject){
        return sqlSource.getBoundSql(parameterObject);
    }

    public static class Builder {
        private MappedStatement mappedStatement = new MappedStatement();

        public Builder(Configuration configuration, String id, SqlCommandType sqlCommandType, SqlSource sqlSource, Class<?> resultType) {
            mappedStatement.configuration = configuration;
            mappedStatement.id = id;
            mappedStatement.sqlCommandType = sqlCommandType;
            mappedStatement.sqlSource = sqlSource;
            mappedStatement.resultType = resultType;
            mappedStatement.keyGenerator = configuration.isUseGeneratedKeys() && SqlCommandType.INSERT.equals(sqlCommandType) ? new Jdbc3KeyGenerator() : new NoKeyGenerator();
            mappedStatement.lang = configuration.getDefaultScriptingLanguageInstance();
        }

        public MappedStatement build(){
            assert mappedStatement.configuration != null;
            assert mappedStatement.id != null;
            mappedStatement.resultMaps = Collections.unmodifiableList(mappedStatement.resultMaps);
            return mappedStatement;
        }

        public Builder cache(Cache cache) {
            mappedStatement.cache = cache;
            return this;
        }

        public Builder flushCacheRequired(boolean flushCacheRequired) {
            mappedStatement.flushCacheRequired = flushCacheRequired;
            return this;
        }

        public Builder useCache(boolean useCache) {
            mappedStatement.useCache = useCache;
            return this;
        }

        public String id(){
            return mappedStatement.id;
        }

        public Builder resultMaps(List<ResultMap> resultMaps){
            mappedStatement.resultMaps = resultMaps;
            return this;
        }

        public Builder keyGenerator(KeyGenerator keyGenerator) {
            mappedStatement.keyGenerator = keyGenerator;
            return this;
        }

        public Builder keyProperty(String keyProperty) {
            mappedStatement.keyProperties = delimitedStringToArray(keyProperty);
            return this;
        }

        public Builder resource(String resource) {
            mappedStatement.resource = resource;
            return this;
        }
    }

    private static String[] delimitedStringToArray(String in) {
        if (in == null || in.trim().length() == 0) {
            return null;
        } else {
            return in.split(",");
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

    public String getResource(){
        return resource;
    }

    public String[] getKeyColumns() {
        return keyColumns;
    }

    public String[] getKeyProperties() {
        return keyProperties;
    }

    public KeyGenerator getKeyGenerator() {
        return keyGenerator;
    }

    public boolean isFlushCacheRequired(){
        return flushCacheRequired;
    }

    public boolean isUseCache() {
        return useCache;
    }

    public Cache getCache() {
        return cache;
    }

}
