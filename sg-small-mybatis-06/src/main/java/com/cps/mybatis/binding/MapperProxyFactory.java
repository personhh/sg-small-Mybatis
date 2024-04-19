package com.cps.mybatis.binding;

import com.cps.mybatis.session.SqlSession;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapperProxyFactory <T>{

    private final Class<T> mapperInterface;

    private Map<Method,MapperMethod> methodCache = new ConcurrentHashMap<Method,MapperMethod>();

    public MapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    public T newInstance(SqlSession sqlSession){
        final MapperProxy<T> mapperProxy = new MapperProxy<>(sqlSession, mapperInterface, methodCache);

        /**   public static Object newProxyInstance(ClassLoader loader,Class<?>[] interfaces,InvocationHandler h) throws IllegalArgumentException
         *   loader: 用哪个类加载器去加载代理对象
         *   interfaces:动态代理类需要实现的接口
         *   h:动态代理方法在执行时，会调用h里面的invoke方法去执行
         */
        return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[]{mapperInterface},mapperProxy);
    }
}
