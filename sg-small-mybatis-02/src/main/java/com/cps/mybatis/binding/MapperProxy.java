package com.cps.mybatis.binding;

import com.cps.mybatis.session.SqlSession;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

public class MapperProxy <T> implements InvocationHandler, Serializable {

    //提供序列化
    private static final long serialversion = -6424540398559729838L;

    private SqlSession sqlSession;

    private final Class<T> mapperInterface;

    public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface) {
        this.sqlSession = sqlSession;
        this.mapperInterface = mapperInterface;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        /**getDeclaringClass
         该方法返回一个Class对象，返回当前class对象的声明对象class,一般针对内部类的情况，
         比如A类有内部类B，那么通过B.class.getDeclaringClass()方法将获取到A的Class对象.*/
        if(Object.class.equals(method.getDeclaringClass())){
            return method.invoke(this,args);
        }else{
            return sqlSession.selectOne(method.getName(), args);
        }
    }
}
