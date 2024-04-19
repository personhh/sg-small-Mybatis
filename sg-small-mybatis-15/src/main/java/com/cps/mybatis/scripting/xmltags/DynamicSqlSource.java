package com.cps.mybatis.scripting.xmltags;

import com.cps.mybatis.builder.SqlSourceBuilder;
import com.cps.mybatis.mapping.BoundSql;
import com.cps.mybatis.mapping.SqlSource;
import com.cps.mybatis.session.Configuration;

import java.util.Map;

/**
 * @author cps
 * @description: 动态sql源码
 * @date 2024/2/21 10:33
 * @OtherDescription: Other things
 */
public class DynamicSqlSource implements SqlSource {

    //配置类
    private Configuration configuration;
    //sql节点
    private SqlNode rootSqlNode;

    public DynamicSqlSource(Configuration configuration, SqlNode sqlNode) {
        this.configuration = configuration;
        this.rootSqlNode = sqlNode;
    }


    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        //生成一个DynamicContext动态上下文
        DynamicContext context = new DynamicContext(configuration, parameterObject);
        //通过SqlNode.apply 将${} 参数替换掉，不替换 #{} 这种参数
        rootSqlNode.apply(context);
        //调用SqlSourceBuilder
        SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
        Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
        //SqlSourceBuilder.parse 这里返回的是 StaticSqlSource，解析过程就把那些参数都替换成?了，也就是最基本的JDBC的SQL语句。
        SqlSource sqlSource =  sqlSourceParser.parse(context.getSql(), parameterType, context.getBindings());
        // SqlSource.getBoundSql，非递归调用，而是调用 StaticSqlSource 实现类
        BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
        for(Map.Entry<String , Object> entry : context.getBindings().entrySet()){
            boundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
        }
        return boundSql;
    }
}
