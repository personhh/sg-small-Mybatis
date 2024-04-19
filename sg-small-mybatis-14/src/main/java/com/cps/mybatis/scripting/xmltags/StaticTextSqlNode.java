package com.cps.mybatis.scripting.xmltags;

/**
 * @author cps
 * @description: TODO
 * @date 2024/1/25 14:41
 * @OtherDescription: Other things
 */
public class StaticTextSqlNode implements SqlNode{

    private String text;

    public StaticTextSqlNode(String text) {
        this.text = text;
    }

    @Override
    public boolean apply(DynamicContext context) {
        //将文本加入context
        context.appendSql(text);
        return true;
    }
}
