package com.cps.mybatis.bindling;

import java.lang.reflect.Proxy;
import java.util.Map;

public class MapperProxyFactory <T>{

    private final Class<T> mapperInterface;

    public MapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    public T newInstance(Map<String, String> sqlSession){
        final MapperProxy<T> mapperProxy = new MapperProxy<>(sqlSession, mapperInterface);

        /**   public static Object newProxyInstance(ClassLoader loader,Class<?>[] interfaces,InvocationHandler h) throws IllegalArgumentException
         *   loader: 用哪个类加载器去加载代理对象
         *   interfaces:动态代理类需要实现的接口
         *   h:动态代理方法在执行时，会调用h里面的invoke方法去执行
         */
        return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[]{mapperInterface},mapperProxy);
    }
}
