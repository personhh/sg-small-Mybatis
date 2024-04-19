package com.cps.mybatis.bindling;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

public class MapperProxy <T> implements InvocationHandler, Serializable {

    //提供序列化
    private static final long serialVersion = -6424540398559729838L;

    //执行sql语句的会话，暂时用map来代替，k -接口名字，v -接口所执行方法的名字
    private Map<String,String> sqlSession;

    //接口类
    private final Class<T> mapperInterface;

    //构造器
    public MapperProxy(Map<String, String> sqlSession, Class<T> mapperInterface) {
        this.sqlSession = sqlSession;
        this.mapperInterface = mapperInterface;
    }

    //invoke执行方法
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        /**getDeclaringClass
         该方法返回一个Class对象，返回当前class对象的声明对象class,一般针对内部类的情况，
         比如A类有内部类B，那么通过B.class.getDeclaringClass()方法将获取到A的Class对象.*/
        if(Object.class.equals(method.getDeclaringClass())){
            return method.invoke(this,args);
        }else{
            return "你的被代理了！" + sqlSession.get(mapperInterface.getName() + "." +method.getName());
        }
    }
}
