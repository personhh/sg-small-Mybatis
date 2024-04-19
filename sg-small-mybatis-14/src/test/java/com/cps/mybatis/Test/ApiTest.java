package com.cps.mybatis.Test;


import com.alibaba.fastjson2.JSON;
import com.cps.mybatis.Test.Dao.IActivityDao;
import com.cps.mybatis.Test.Po.Activity;
import com.cps.mybatis.builder.xml.XMLConfigBuilder;
import com.cps.mybatis.executor.Executor;
import com.cps.mybatis.io.Resources;
import com.cps.mybatis.mapping.Environment;
import com.cps.mybatis.session.*;
import com.cps.mybatis.session.defaults.DefaultSqlSession;
import com.cps.mybatis.transaction.Transaction;
import com.cps.mybatis.transaction.TransactionFactory;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;

public class ApiTest {

    private Logger logger = LoggerFactory.getLogger(ApiTest.class);

    private SqlSession sqlSession;

    @Before
    public void init() throws IOException {
        // 1. 从SqlSessionFactory中获取SqlSession
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis-config-datasource.xml"));
        sqlSession = sqlSessionFactory.openSession();
    }

    @Test
    public void test_queryActivityById() {
        // 1. 获取映射器对象
        IActivityDao dao = sqlSession.getMapper(IActivityDao.class);
        // 2. 测试验证
        Activity res = dao.queryActivityById(100001L);
        logger.info("测试结果：{}", JSON.toJSONString(res));
    }

    @Test
    public void test_insert() {
        // 1. 获取映射器对象
        IActivityDao dao = sqlSession.getMapper(IActivityDao.class);

        Activity activity = new Activity();
        activity.setActivityId(10008L);
        activity.setActivityName("测试活动");
        activity.setActivityDesc("测试数据插入");


        // 2. 测试验证
        Integer res = dao.insert(activity);
        sqlSession.commit();

        logger.info("测试结果：count：{} idx：{}", res, JSON.toJSONString(activity.getId()));
    }

}
