package com.cps.mybatis.scripting;

import com.cps.mybatis.executor.parameter.ParameterHandler;
import com.cps.mybatis.mapping.BoundSql;
import com.cps.mybatis.mapping.MappedStatement;
import com.cps.mybatis.mapping.SqlSource;
import com.cps.mybatis.session.Configuration;
import org.dom4j.Element;

/**
 * @description: 脚本语言驱动器
 * @author cps
 * @date 2024/1/25 12:50
 * @OtherDescription: Other things
 */
public interface LanguageDriver {

    //创建SQL源码（mapper xml方式）
    SqlSource createSqlSource(Configuration configuration, Element script, Class<?> parameterType);

    //创建参数处理器
    ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql);

    //创建SQL源码（注解方式）
    SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType);


}
