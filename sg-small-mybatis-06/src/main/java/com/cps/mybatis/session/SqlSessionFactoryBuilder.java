package com.cps.mybatis.session;


import com.cps.mybatis.builder.xml.XMLConfigBuilder;
import com.cps.mybatis.session.defaults.DefaultSqlSessionFactory;

import java.io.Reader;

public class SqlSessionFactoryBuilder {

    public SqlSessionFactory build(Reader reader){
        XMLConfigBuilder xmlConfigBuilder = new XMLConfigBuilder(reader);
        return build(xmlConfigBuilder.parse());
    }

    public SqlSessionFactory build(Configuration config){
        return new DefaultSqlSessionFactory(config);
    }
}
