package com.cps.mybatis.Test;


import com.alibaba.fastjson2.JSON;
import com.cps.mybatis.Test.Dao.IUserDao;
import com.cps.mybatis.Test.Po.User;
import com.cps.mybatis.io.Resources;
import com.cps.mybatis.session.SqlSession;
import com.cps.mybatis.session.SqlSessionFactory;
import com.cps.mybatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ApiTest {

    private Logger logger = LoggerFactory.getLogger(ApiTest.class);

    @Test
    public void test_SqlSessionFactory() throws IOException{
        //1.从SqlSessionFactory中获取SqlSession
        Reader resourceAsReader = Resources.getResourceAsReader("mybatis-config-datasource.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsReader);
        SqlSession sqlSession = sqlSessionFactory.openSession();

        //2.获取映射器对象
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);

        //3.测试验证
        User user = userDao.queryUserInfoById(1L);
        logger.info("测试结果：{}", JSON.toJSONString(user));
    }

    //对反射的测试
    @Test
    public void testClass() throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        System.out.println(Class.forName("com.cps.mybatis.Test.Po.User"));//class com.cps.mybatis.Test.Po.User
        Class<?> clazz = Class.forName("com.cps.mybatis.Test.Po.User");
        Object obj = clazz.newInstance();
        System.out.println(obj);
        Class<?> aClass = obj.getClass();
        Method method = clazz.getMethod("setId",Long.class);
        System.out.println(method);//public void com.cps.mybatis.Test.Po.User.setId(java.lang.Long)
        method.invoke(obj, 123L);
        Method method1 = clazz.getMethod("getId");
        method1.invoke(obj);


    }
}
