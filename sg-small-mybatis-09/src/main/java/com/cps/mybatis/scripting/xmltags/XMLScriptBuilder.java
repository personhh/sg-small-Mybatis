package com.cps.mybatis.scripting.xmltags;

import com.cps.mybatis.builder.BaseBuilder;
import com.cps.mybatis.mapping.SqlSource;
import com.cps.mybatis.scripting.defaults.RawSqlSource;
import com.cps.mybatis.session.Configuration;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cps
 * @description: xml脚本构造器
 * @date 2024/1/25 14:11
 * @OtherDescription: Other things
 */
public class XMLScriptBuilder extends BaseBuilder {

    private Element element;
    private boolean isDynamic;
    private Class<?> parameterType;

    public XMLScriptBuilder(Configuration configuration, Element element, Class<?> parameterType) {
        super(configuration);
        this.element = element;
        this.parameterType = parameterType;
    }

    public SqlSource parseScriptNode(){
        List<SqlNode> contents = parseDynamicTags(element);
        MixedSqlNode rootSqlNode = new MixedSqlNode(contents);
        return new RawSqlSource(configuration, rootSqlNode, parameterType);
    }

    List<SqlNode> parseDynamicTags(Element element){
        List<SqlNode> contents = new ArrayList<>();
        //element.getText 拿到SQL
        String data = element.getText();
        contents.add(new StaticTextSqlNode(data));
        return contents;
    }
}
