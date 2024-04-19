package com.cps.mybatis.scripting.xmltags;

/**
 * @author cps
 * @description: SQL 节点
 * @date 2024/1/25 14:16
 * @OtherDescription: Other things
 */
public interface SqlNode {

    boolean apply(DynamicContext context);
}
