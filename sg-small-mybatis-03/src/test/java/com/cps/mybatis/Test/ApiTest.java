package com.cps.mybatis.Test;


import com.cps.mybatis.Test.Dao.IUserDao;
import com.cps.mybatis.Test.Po.User;
import com.cps.mybatis.io.Resources;
import com.cps.mybatis.session.SqlSession;
import com.cps.mybatis.session.SqlSessionFactory;
import com.cps.mybatis.session.SqlSessionFactoryBuilder;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        String res = userDao.queryUserInfoById("10001");
        logger.info("测试结果：{}",res);
    }

    @Test
    public void ClassNameTest() throws ClassNotFoundException {
        Class<?> user = Class.forName("com.cps.mybatis.Test.Po.User");
        System.out.println(user.getClassLoader());
        System.out.println(user);
        System.out.println(Thread.currentThread().getContextClassLoader());
        System.out.println(ClassLoader.getSystemClassLoader());
    }

    @Test
    public void InputStreamTest(){
        //根路径下使用getResourceAsStream方法
        InputStream resourceAsStream = ApiTest.class.getClassLoader().getResourceAsStream("mybatis-config-datasource.xml");
        System.out.println(resourceAsStream);
    }

    @Test
    public void SAXReaderTest() throws IOException {
        InputStream resourceAsStream = ApiTest.class.getClassLoader().getResourceAsStream("mybatis-config-datasource.xml");
        Reader resourceAsReader = new InputStreamReader(resourceAsStream);
        //Reader resourceAsReader = Resources.getResourceAsReader("mybatis-config-datasource.xml");
        SAXReader saxReader = new SAXReader();
        try {
            //Document document = saxReader.read(new File("/Users/cps/IdeaProjects/sg-small-mybatis/sg-small-mybatis-03/src/test/resources/mybatis-config-datasource.xml"));
            Document document = saxReader.read(new InputSource(resourceAsReader));
            System.out.println(document);//org.dom4j.tree.DefaultDocument@641147d0 [Document: name null]
            Element rootElement = document.getRootElement();
            System.out.println(rootElement);//org.dom4j.tree.DefaultElement@6e38921c [Element: <configuration attributes: []/>]
            Element mappers = rootElement.element("mappers");
            System.out.println(mappers);//org.dom4j.tree.DefaultElement@64d7f7e0 [Element: <mappers attributes: []/>]
            Element mapper = mappers.element("mapper");
            System.out.println(mapper);//org.dom4j.tree.DefaultElement@27c6e487 [Element: <mapper attributes: [org.dom4j.tree.DefaultAttribute@49070868 [Attribute: name resource value "mapper/User_Mapper.xml"]]/>]
            String resource = mapper.attributeValue("resource");
            System.out.println(resource);//mapper/User_Mapper.xml


        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

}
