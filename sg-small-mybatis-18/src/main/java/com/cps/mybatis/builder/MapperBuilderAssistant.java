package com.cps.mybatis.builder;

import com.cps.mybatis.cache.Cache;
import com.cps.mybatis.cache.decorators.FifoCache;
import com.cps.mybatis.cache.impl.PerpetualCache;
import com.cps.mybatis.executor.keygen.KeyGenerator;
import com.cps.mybatis.mapping.*;
import com.cps.mybatis.reflection.MetaClass;
import com.cps.mybatis.scripting.LanguageDriver;
import com.cps.mybatis.session.Configuration;
import com.cps.mybatis.type.TypeHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author cps
 * @description: 映射构建器助手，建造者
 * @date 2024/1/26 20:43
 * @OtherDescription: MapperBuilderAssistant 构建器助手专门为创建 MappedStatement 映射语句类而服务的，
 * 在这个类中封装了入参和出参的映射、以及把这些配置信息写入到 Configuration 配置项中。
 */
public class MapperBuilderAssistant extends BaseBuilder{

    private String currentNameSpace;//当前命名空间
    private String resource;
    private Cache currentCache;

    public MapperBuilderAssistant(Configuration configuration, String resource) {
        super(configuration);
        this.resource = resource;
    }


    public String getCurrentNameSpace() {
        return currentNameSpace;
    }

    public void setCurrentNameSpace(String currentNameSpace) {
        this.currentNameSpace = currentNameSpace;
    }

    //添加namespace前缀
    public String applyCurrentNamespace(String base, boolean isReference){
        if(base == null){
            return null;
        }
        if(isReference){
            if(base.contains(".")){
                return base;
            }
        }
        else {
            if(base.startsWith(currentNameSpace + ".")){
                return base;
            }
            if(base.contains(".")){
                throw new RuntimeException("Dots are not allowed in element names, please remove it from " + base);
            }
        }
        return currentNameSpace + "." + base;
    }
    //添加映射器语句
    public MappedStatement addMappedStatement(String id,
                                              SqlSource sqlSource,
                                              SqlCommandType sqlCommandType,
                                              Class<?> parameterType,
                                              String resultMap,
                                              Class<?> resultType,
                                              boolean flushCache,
                                              boolean useCache,
                                              KeyGenerator keyGenerator,
                                              String keyProperty,
                                              LanguageDriver lang){
        //给id加上namespace前缀
        id = applyCurrentNamespace(id,false);

        boolean isSelect = sqlCommandType == SqlCommandType.SELECT;
        //生成mapperStatementBuilder对象
        MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration, id, sqlCommandType, sqlSource, resultType);
        statementBuilder.resource(resource);
        statementBuilder.keyGenerator(keyGenerator);
        statementBuilder.keyProperty(keyProperty);
        //进行结果映射，给MappedStatement添加resultMaps
        setStatementResultMap(resultMap, resultType, statementBuilder);
        setStatementCache(isSelect, flushCache, useCache, currentCache, statementBuilder);
        MappedStatement statement = statementBuilder.build();
        //映射语句信息。建造完存放到配置中
        configuration.addMappedStatement(statement);

        return statement;

    }

    private void setStatementCache(
            boolean isSelect,
            boolean flushCache,
            boolean useCache,
            Cache cache,
            MappedStatement.Builder statementBuilder) {
        flushCache = valueOrDefault(flushCache, !isSelect);
        useCache = valueOrDefault(useCache, isSelect);
        statementBuilder.flushCacheRequired(flushCache);
        statementBuilder.useCache(useCache);
        statementBuilder.cache(cache);
    }


    //结果映射，设置语句结果集
    private void setStatementResultMap(String resultMap, Class<?> resultType, MappedStatement.Builder statementBuilder) {
        //因为目前值处理resultType，不处理resultMap，所以这个地方返回的是null
        resultMap = applyCurrentNamespace(resultMap,true);

        List<ResultMap> resultMaps = new ArrayList<>();

        //对返回结果集进行判断
        if(resultMap != null){
            String[] resultMapNames = resultMap.split(",");
            for(String resultMapName : resultMapNames){
                resultMaps.add(configuration.getResultMap(resultMapName.trim()));
            }
        }

        //一般使用resultType作为返回内容处理就行
        else if(resultType != null){
             ResultMap.Builder inlineResultMapBuilder = new ResultMap.Builder(
                    configuration,
                    statementBuilder.id() + "-Inline",
                    resultType,
                    new ArrayList<>()
            );
            resultMaps.add(inlineResultMapBuilder.build());
        }
        statementBuilder.resultMaps(resultMaps);
    }


    public ResultMap addResultMap(String id, Class<?> type, List<ResultMapping> resultMappings){
        id = applyCurrentNamespace(id,false);
        ResultMap.Builder inlineResultMapBuilder = new ResultMap.Builder(configuration,id,type,resultMappings);

        ResultMap resultMap = inlineResultMapBuilder.build();
        configuration.addResultMap(resultMap);
        return resultMap;
    }
    public ResultMapping buildResultMapping(Class<?> resultType, String property, String column, List<ResultFlag> flags){
        Class<?> javaTypeClass = resolveResultJavaType(resultType, property, null);
        TypeHandler<?> typeHandlerInstance = resolveTypeHandler(javaTypeClass, null);
        ResultMapping.Builder builder = new ResultMapping.Builder(configuration, property, column, javaTypeClass);
        builder.typeHandler(typeHandlerInstance);
        builder.flags(flags);

        return builder.build();
    }

    private Class<?> resolveResultJavaType(Class<?> resultType, String property, Class<?> javaType){
        if(javaType == null && property != null){
            try{
                MetaClass metaResultType = MetaClass.forClass(resultType);
                javaType = metaResultType.getSetterType(property);
            }catch (Exception ignore){

            }
        }
        if(javaType == null){
            javaType = Object.class;
        }
        return javaType;
    }

    public Cache useNewCache(Class<? extends Cache> typeClass,
                             Class<? extends Cache> evictionClass,
                             Long flushInterval,
                             Integer size,
                             boolean readWrite,
                             boolean blocking,
                             Properties props) {
        // 判断为null，则用默认值
        typeClass = valueOrDefault(typeClass, PerpetualCache.class);
        evictionClass = valueOrDefault(evictionClass, FifoCache.class);

        // 建造者模式构建 Cache [currentNamespace=com.cps.mybatis.test.dao.IActivityDao]
        Cache cache = new CacheBuilder(currentNameSpace)
                .implementation(typeClass)
                .addDecorator(evictionClass)
                .clearInterval(flushInterval)
                .size(size)
                .readWrite(readWrite)
                .blocking(blocking)
                .properties(props)
                .build();

        // 添加缓存
        configuration.addCache(cache);
        currentCache = cache;
        return cache;
    }

    private <T> T valueOrDefault(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

}
