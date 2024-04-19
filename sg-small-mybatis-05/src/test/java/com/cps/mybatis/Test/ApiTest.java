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
        for(int i = 0; i < 50 ; i++){
            User user = userDao.queryUserInfoById(1L);
            logger.info("测试结果：{}", JSON.toJSONString(user));
        }
    }

    @Test
    public void ClassTest() throws ClassNotFoundException {
        Class<?> aClass = Class.forName("com.cps.mybatis.Test.Po.User");
        String name = aClass.getClass().getName();
        System.out.println(name);
        String name1 = int.class.getName();
        System.out.println(name1);
    }
}
