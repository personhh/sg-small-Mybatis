package com.cps.mybatis.mapping;

/**
 * @author cps
 * @description: SQL源码
 * @date 2024/1/25 12:52
 * @OtherDescription: 定义脚本语言驱动接口，提供创建 SQL 信息的方法，入参包括了配置、元素、参数
 */
public interface SqlSource {

    BoundSql getBoundSql(Object parameterObject);
}
