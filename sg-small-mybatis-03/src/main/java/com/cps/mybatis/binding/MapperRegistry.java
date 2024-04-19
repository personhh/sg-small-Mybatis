package com.cps.mybatis.binding;

import cn.hutool.core.lang.ClassScanner;
import com.cps.mybatis.session.Configuration;
import com.cps.mybatis.session.SqlSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MapperRegistry {

    private Configuration config;

    public MapperRegistry(Configuration config) {
        this.config = config;
    }

    /**
     * 将已添加的映射器代理加入到HashMap
     * */

    public final Map<Class<?>, MapperProxyFactory<?>> knowMappers = new HashMap<>();

    public <T> T getMapper(Class<T> type, SqlSession sqlSession){
        final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knowMappers.get(type);
        if(mapperProxyFactory == null){
            //如果对应的代理工厂没找到
            throw new RuntimeException("Type" + type + " is not known to the MapperRegistry.");
        }
        try{
            return mapperProxyFactory.newInstance(sqlSession);
        }catch (Exception e){
            throw new RuntimeException("Error getting mapper instance. Cause: " + e, e);
        }
    }

    public <T> void addMapper(Class<T> type){
        if(type.isInterface()){
            //判断类型是不是接口
            if(hasMapper(type)){
                //如果重复添加了，报错
                throw new RuntimeException("Type" + type + "is not known to the MapperRegistry.");
            }
            //注册映射器代理工厂
            knowMappers.put(type,new MapperProxyFactory<>(type));
        }
    }

    public <T> boolean hasMapper(Class<T> type){
        return knowMappers.containsKey(type);
    }

    public void addMappers(String packageName){
        Set<Class<?>> mapperSet = ClassScanner.scanPackage(packageName);
        for(Class<?> mapperClass : mapperSet){
            addMapper(mapperClass);
        }
    }

}
