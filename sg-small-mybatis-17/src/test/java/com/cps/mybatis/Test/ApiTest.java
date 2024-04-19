package com.cps.mybatis.Test;


import com.alibaba.fastjson2.JSON;
import com.cps.mybatis.Test.Dao.IActivityDao;
import com.cps.mybatis.Test.Po.Activity;
import com.cps.mybatis.io.Resources;
import com.cps.mybatis.session.SqlSession;
import com.cps.mybatis.session.SqlSessionFactory;
import com.cps.mybatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ApiTest {
    private Logger logger = LoggerFactory.getLogger(ApiTest.class);


    @Test
    public void test_queryActivityById() throws IOException {
        // 1. 从SqlSessionFactory中获取SqlSession
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis-config-datasource.xml"));
        SqlSession sqlSession = sqlSessionFactory.openSession();

        // 2. 获取映射器对象
        IActivityDao dao = sqlSession.getMapper(IActivityDao.class);

        // 3. 测试验证
        Activity req = new Activity();
        req.setActivityId(100001L);
        logger.info("测试结果：{}", JSON.toJSONString(dao.queryActivityById(req)));

        /*sqlSession.commit();
        sqlSession.clearCache();
        sqlSession.close();*/
        logger.info("测试结果：{}", JSON.toJSONString(dao.queryActivityById(req)));
    }
}
