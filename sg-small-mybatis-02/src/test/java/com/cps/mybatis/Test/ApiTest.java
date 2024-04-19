package com.cps.mybatis.Test;

import com.cps.mybatis.Test.Dao.ISchoolDao;
import com.cps.mybatis.binding.MapperRegistry;
import com.cps.mybatis.session.SqlSession;
import com.cps.mybatis.session.defaults.DefaultSqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ApiTest {

    private Logger logger = LoggerFactory.getLogger(ApiTest.class);

    @Test
    public void test_MapperProxyFactory(){
        //1.注册mapper
        MapperRegistry mapperRegistry = new MapperRegistry();
        mapperRegistry.addMappers("com.cps.mybatis.Test.Dao");

        //2.从SqlSession 工厂获取Session
        DefaultSqlSessionFactory sqlSessionFactory = new DefaultSqlSessionFactory(mapperRegistry);
        SqlSession sqlSession = sqlSessionFactory.openSession();

        //3.获取映射器对象
        ISchoolDao schoolDao = sqlSession.getMapper(ISchoolDao.class);

        //4.测试验证

        String res = schoolDao.querySchoolName("1000");
        logger.info("测试结果：{}" , res);
    }
}
