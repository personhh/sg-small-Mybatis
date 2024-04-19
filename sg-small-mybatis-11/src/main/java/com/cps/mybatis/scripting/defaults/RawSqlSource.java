package com.cps.mybatis.scripting.defaults;

import com.cps.mybatis.builder.SqlSourceBuilder;
import com.cps.mybatis.mapping.BoundSql;
import com.cps.mybatis.mapping.SqlSource;
import com.cps.mybatis.scripting.xmltags.DynamicContext;
import com.cps.mybatis.scripting.xmltags.SqlNode;
import com.cps.mybatis.session.Configuration;

import java.util.HashMap;

/**
 * @author cps
 * @description: 原始sql源码，比DynamicSqlSource 动态sql处理快
 * @date 2024/1/25 14:30
 * @OtherDescription: Other things
 */
public class RawSqlSource implements SqlSource {

    private final SqlSource sqlSource;

    public RawSqlSource(Configuration configuration, SqlNode rootSqlNode, Class<?> parameterType) {
        this(configuration, getSql(configuration, rootSqlNode), parameterType);
    }

    public RawSqlSource(Configuration configuration, String sql,Class<?> parameterType){
        SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
        Class<?> clazz = parameterType == null ? Object.class : parameterType;
        sqlSource = sqlSourceParser.parse(sql,clazz,new HashMap<>());
    }


    private static String getSql(Configuration configuration, SqlNode rootSqlNode) {
        DynamicContext context = new DynamicContext(configuration, null);
        rootSqlNode.apply(context);
        return context.getSql();
    }


    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return sqlSource.getBoundSql(parameterObject);
    }


}
