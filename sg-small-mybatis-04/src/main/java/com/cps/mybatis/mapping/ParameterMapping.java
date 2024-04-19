package com.cps.mybatis.mapping;


import com.cps.mybatis.session.Configuration;
import com.cps.mybatis.type.JdbcType;

import java.sql.JDBCType;

//参数映射
public class ParameterMapping {
    private Configuration configuration;

    //参数属性名称
    private String property;
    //参数属性类型
    private Class<?> javaType = Object.class;
    //参数属性jdbc类型，也就是这个属性对应在数据库表的属性是什么类型，比如char、varChar
    private JdbcType jdbcType;

    public ParameterMapping() {
    }

    public static class Builder {

        private ParameterMapping parameterMapping = new ParameterMapping();

        public Builder(Configuration configuration, String property) {
            parameterMapping.configuration = configuration;
            parameterMapping.property = property;
        }

        public Builder javaType(Class<?> javaType) {
            parameterMapping.javaType = javaType;
            return this;
        }

        public Builder jdbcType(JdbcType jdbcType) {
            parameterMapping.jdbcType = jdbcType;
            return this;
        }

    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public String getProperty() {
        return property;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public JdbcType getJdbcType() {
        return jdbcType;
    }

}
