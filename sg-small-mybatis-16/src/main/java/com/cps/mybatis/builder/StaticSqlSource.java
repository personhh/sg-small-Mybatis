package com.cps.mybatis.builder;

import com.cps.mybatis.mapping.BoundSql;
import com.cps.mybatis.mapping.ParameterMapping;
import com.cps.mybatis.mapping.SqlSource;
import com.cps.mybatis.session.Configuration;

import java.util.List;

/**
 * @author cps
 * @description: 静态SQL源码
 * @date 2024/1/25 17:21
 * @OtherDescription: Other things
 */
public class StaticSqlSource implements SqlSource {

    private String sql;
    private List<ParameterMapping> parameterMappings;
    private Configuration configuration;

    public StaticSqlSource(Configuration configuration, String sql) {
        this(configuration, sql, null);
    }

    public StaticSqlSource(Configuration configuration, String sql, List<ParameterMapping> parameterMappings){
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.configuration = configuration;
    }


    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return new BoundSql(configuration, sql, parameterMappings, parameterObject);
    }
}
