package com.cps.mybatis.Test;


import com.alibaba.fastjson2.JSON;
import com.cps.mybatis.Test.Dao.IActivityDao;
import com.cps.mybatis.Test.Po.Activity;
import com.cps.mybatis.io.Resources;
import com.cps.mybatis.session.SqlSession;
import com.cps.mybatis.session.SqlSessionFactory;
import com.cps.mybatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

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
    public void test_queryActivityById(){
        //1.获取映射器对象
        IActivityDao dao = sqlSession.getMapper(IActivityDao.class);
        //2.测试验证
        Activity res = dao.queryActivityById(100001L);
        System.out.println(res);
        logger.info("测试结果：{}" , JSON.toJSONString(res));
    }
}
