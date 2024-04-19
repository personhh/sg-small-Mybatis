package com.cps.mybatis.session;


//配置项


import com.cps.mybatis.binding.MapperRegistry;
import com.cps.mybatis.datasource.druid.DruidDataSourceFactory;
import com.cps.mybatis.datasource.pooled.PooledDataSourceFactory;
import com.cps.mybatis.datasource.unpooled.UnpooledDataSourceFactory;
import com.cps.mybatis.executor.Executor;
import com.cps.mybatis.executor.SimpleExecutor;
import com.cps.mybatis.executor.keygen.KeyGenerator;
import com.cps.mybatis.executor.parameter.ParameterHandler;
import com.cps.mybatis.executor.resultset.DefaultResultSetHandler;
import com.cps.mybatis.executor.resultset.ResultSetHandler;
import com.cps.mybatis.executor.statement.PreparedStatementHandler;
import com.cps.mybatis.executor.statement.StatementHandler;
import com.cps.mybatis.mapping.BoundSql;
import com.cps.mybatis.mapping.Environment;
import com.cps.mybatis.mapping.MappedStatement;
import com.cps.mybatis.mapping.ResultMap;
import com.cps.mybatis.plugin.Interceptor;
import com.cps.mybatis.plugin.InterceptorChain;
import com.cps.mybatis.reflection.MetaObject;
import com.cps.mybatis.reflection.factory.DefaultObjectFactory;
import com.cps.mybatis.reflection.factory.ObjectFactory;
import com.cps.mybatis.reflection.wrapper.DefaultObjectWrapperFactory;
import com.cps.mybatis.reflection.wrapper.ObjectWrapperFactory;
import com.cps.mybatis.scripting.LanguageDriver;
import com.cps.mybatis.scripting.LanguageDriverRegistry;
import com.cps.mybatis.scripting.xmltags.XMLLanguageDriver;
import com.cps.mybatis.session.defaults.RowBounds;
import com.cps.mybatis.transaction.Transaction;
import com.cps.mybatis.transaction.jdbc.JdbcTransactionFactory;
import com.cps.mybatis.type.TypeAliasRegistry;
import com.cps.mybatis.type.TypeHandlerRegistry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Configuration {

    //环境
    protected Environment environment;
    protected boolean useGeneratedKeys = false;

    // 插件拦截器链
    protected final InterceptorChain interceptorChain = new InterceptorChain();

    //映射注册机
    protected MapperRegistry mapperRegistry = new MapperRegistry(this);

    //映射的语句 存在map里
    protected  final Map<String, MappedStatement> mappedStatementMap = new HashMap<>();
    //结果映射，存在Map里
    protected final Map<String, ResultMap> resultMaps = new HashMap<>();

    protected final Map<String, KeyGenerator> keyGenerators = new HashMap<>();
    //类型别名注册机
    protected final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();
    //类型处理器注册机
    protected final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();
    //默认的语言驱动器
    protected final LanguageDriverRegistry languageRegistry = new LanguageDriverRegistry();

    //对象工厂和对象包装工厂
    protected ObjectFactory objectFactory = new DefaultObjectFactory();
    protected ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();

    protected String databaseId;
    protected final Set<String> loadedResources = new HashSet<>();

    public Configuration() {
        typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);
        typeAliasRegistry.registerAlias("DRUID", DruidDataSourceFactory.class);
        typeAliasRegistry.registerAlias("UNPOOLED", UnpooledDataSourceFactory.class);
        typeAliasRegistry.registerAlias("POOLED", PooledDataSourceFactory.class);

        languageRegistry.setDefaultDriverClass(XMLLanguageDriver.class);
    }

    public void addMappers(String packageName){
        mapperRegistry.addMappers(packageName);
    }

    public <T> void addMapper(Class<T> type){
        mapperRegistry.addMapper(type);
    }

    public <T> T getMapper(Class<T> type, SqlSession sqlSession){
        return mapperRegistry.getMapper(type,sqlSession);
    }

    public boolean hasMapper(Class<?> type){
        return mapperRegistry.hasMapper(type);
    }

    public void addMappedStatement(MappedStatement ms){
        mappedStatementMap.put(ms.getId(),ms);
    }

    public MappedStatement getMappedStatement(String id){
        return mappedStatementMap.get(id);
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public Map<String, MappedStatement> getMappedStatementMap() {
        return mappedStatementMap;
    }

    public TypeAliasRegistry getTypeAliasRegistry() {
        return typeAliasRegistry;
    }

    /**
     * 创建结果集处理器
     * */
    public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement, ResultHandler resultHandler, RowBounds rowBounds, BoundSql boundSql){
        return new DefaultResultSetHandler(executor, mappedStatement, resultHandler, rowBounds, boundSql);
    }

    /**
     * 生产执行器
     */
    public Executor newExecutor(Transaction transaction) {
        return new SimpleExecutor(this, transaction);
    }

    /**
     * 创建（获取）语句处理器
     */
    public StatementHandler newStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameter, ResultHandler resultHandler, RowBounds rowBounds, BoundSql boundSql) {
        // 创建语句处理器，Mybatis 这里加了路由 STATEMENT、PREPARED、CALLABLE 我们默认只根据预处理进行实例化
        StatementHandler statementHandler = new PreparedStatementHandler(executor, mappedStatement, parameter, resultHandler, rowBounds, boundSql);
        // 嵌入插件，代理对象
        statementHandler = (StatementHandler) interceptorChain.pluginAll(statementHandler);
        return statementHandler;

    }

    public boolean isResourceLoaded(String resource){
        return loadedResources.contains(resource);
    }

    public void addLoadedResource(String resource) {
        loadedResources.add(resource);
    }
    public LanguageDriverRegistry getLanguageRegistry(){
        return languageRegistry;
    }

    public MetaObject newMetaObject(Object object){
        return MetaObject.forObject(object,objectFactory,objectWrapperFactory);
    }

    public String getDatabaseId(){
        return databaseId;
    }

    public TypeHandlerRegistry getTypeHandlerRegistry() {
        return typeHandlerRegistry;
    }

    public LanguageDriver getDefaultScriptingLanguageInstance(){
        return languageRegistry.getDefaultDriver();
    }

    public ParameterHandler newParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql){
        //构建参数处理器
        ParameterHandler parameterHandler = mappedStatement.getLang().createParameterHandler(mappedStatement, parameterObject, boundSql);
        return parameterHandler;
    }

    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    public ResultMap getResultMap(String id) {
        return resultMaps.get(id);
    }

    public void addResultMap(ResultMap resultMap){
        resultMaps.put(resultMap.getId(), resultMap);
    }

    public void addKeyGenerator(String id, KeyGenerator keyGenerator) {
        keyGenerators.put(id, keyGenerator);
    }

    public KeyGenerator getKeyGenerator(String id) {
        return keyGenerators.get(id);
    }

    public boolean hasKeyGenerator(String id) {
        return keyGenerators.containsKey(id);
    }

    public boolean isUseGeneratedKeys() {
        return useGeneratedKeys;
    }

    public void setUseGeneratedKeys(boolean useGeneratedKeys) {
        this.useGeneratedKeys = useGeneratedKeys;
    }

    public void addInterceptor(Interceptor interceptorInstance) {
        interceptorChain.addInterceptor(interceptorInstance);
    }


}
