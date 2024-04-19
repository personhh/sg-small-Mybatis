package com.cps.mybatis.mapping;

import com.cps.mybatis.session.Configuration;
import com.cps.mybatis.type.JdbcType;
import com.cps.mybatis.type.TypeHandler;

/**
 * @author cps
 * @description: 结果映射
 * @date 2024/1/26 20:37
 * @OtherDescription: Other things
 */
public class ResultMapping {

    private Configuration configuration;
    private Class<?> javaType;
    private JdbcType jdbcType;
    private String property;
    private String column;
    private TypeHandler<?> typeHandler;

    public ResultMapping() {
    }

    public static class Builder{
        private ResultMapping resultMapping = new ResultMapping();
    }
}
