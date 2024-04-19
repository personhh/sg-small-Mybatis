package com.cps.mybatis.session;


//配置项

import com.cps.mybatis.binding.MapperRegistry;
import com.cps.mybatis.mapping.MappedStatement;

import java.util.HashMap;
import java.util.Map;

public class Configuration {

    //映射注册机
    protected MapperRegistry mapperRegistry = new MapperRegistry(this);

    //映射的语句 存在map里
    protected  final Map<String, MappedStatement> mappedStatementMap = new HashMap<>();

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

}
