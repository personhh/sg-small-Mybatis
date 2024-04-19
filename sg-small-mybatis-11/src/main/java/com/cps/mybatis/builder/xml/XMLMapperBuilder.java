package com.cps.mybatis.builder.xml;

import com.cps.mybatis.builder.BaseBuilder;
import com.cps.mybatis.builder.MapperBuilderAssistant;
import com.cps.mybatis.io.Resources;
import com.cps.mybatis.session.Configuration;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.List;

/**
 * @author cps
 * @description: TODO
 * @date 2024/1/25 09:57
 * @OtherDescription: Other things
 */
public class XMLMapperBuilder extends BaseBuilder {

    private Element element;
    private String resource;
    private MapperBuilderAssistant mapperBuilderAssistant;

    public XMLMapperBuilder(InputStream inputStream,Configuration configuration, String resource) throws DocumentException {
        this(new SAXReader().read(inputStream), configuration, resource);
    }

    private XMLMapperBuilder(Document document, Configuration configuration, String resource){
        super(configuration);
        this.element = document.getRootElement();
        this.resource = resource;
        this.mapperBuilderAssistant = new MapperBuilderAssistant(configuration, resource);
    }

    /**
     * 解析
     * */
    public void parse() throws Exception{
        //判断资源有没有加载过，防止重复加载
        if(!configuration.isResourceLoaded(resource)){
            configurationElement(element);
            //标记一下，已经加载过啦
            configuration.addLoadedResource(resource);
            //绑定映射器到namespace
            configuration.addMapper(Resources.classForName(mapperBuilderAssistant.getCurrentNameSpace()));
        }
    }

    private void configurationElement(Element element) {
        //1.配置namespace，检查为不为空
        String currentNamespace = element.attributeValue("namespace");
        if(currentNamespace.equals("")){
            throw new RuntimeException("Mapper's namespace cannot be empty");
        }

        mapperBuilderAssistant.setCurrentNameSpace(currentNamespace);
        //2.配置select｜insert｜update｜delete  暂时实现select
        buildStatementFromContext(element.elements("select"),
                element.elements("insert"),
                element.elements("update"),
                element.elements("delete"));
    }

    //配置select｜insert｜update｜delete
    @SafeVarargs
    private final void buildStatementFromContext(List<Element>... lists) {
        //遍历配置处理
        for(List<Element> list: lists)
        for(Element element : list){
            final XMLStatementBuilder statementParser = new XMLStatementBuilder(configuration,mapperBuilderAssistant,element);
            statementParser.parseStatementNode();
        }
    }
}
