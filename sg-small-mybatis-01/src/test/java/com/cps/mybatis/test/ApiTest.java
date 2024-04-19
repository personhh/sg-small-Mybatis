package com.cps.mybatis.test;

import com.cps.mybatis.bindling.MapperProxyFactory;
import com.cps.mybatis.test.dao.IUserDao;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ApiTest {
    private Logger logger = LoggerFactory.getLogger(ApiTest.class);
    @Test
    public void test_MapperProxyFactory(){
        MapperProxyFactory<IUserDao> factory = new MapperProxyFactory<>(IUserDao.class);
        Map<String, String> sqlSession = new HashMap<>();

        sqlSession.put("com.cps.mybatis.test.dao.IUserDao.queryUserName","模拟执行 Mapper.xml 中 SQL 语句的操作：查询用户姓名");
        sqlSession.put("com.cps.mybatis.test.dao.IUserDao.queryUserAge","模拟执行 Mapper.xml 中 SQL 语句的操作：查询用户年龄");
        IUserDao userDao = factory.newInstance(sqlSession);
        String queryUserName = userDao.queryUserName("100");
        logger.info("测试结果：{}" , queryUserName);
    }

    @Test
    public void test_proxy_class(){
        IUserDao userDao = (IUserDao) Proxy.newProxyInstance(IUserDao.class.getClassLoader(), new Class[]{IUserDao.class}, ((proxy, method, args) -> "你被代理啦！"));
        String result = userDao.queryUserName("100001");
        System.out.println("测试结果：" + result);
    }
}
