package com.cps.mybatis.builder.xml;

import com.cps.mybatis.builder.BaseBuilder;
import com.cps.mybatis.builder.MapperBuilderAssistant;
import com.cps.mybatis.builder.ResultMapResolver;
import com.cps.mybatis.cache.Cache;
import com.cps.mybatis.io.Resources;
import com.cps.mybatis.mapping.ResultFlag;
import com.cps.mybatis.mapping.ResultMap;
import com.cps.mybatis.mapping.ResultMapping;
import com.cps.mybatis.session.Configuration;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

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

        //2.配置cache
         cacheElement(element.element("cache"));
        //2.解析resultMap 新增
        resultMapElements(element.elements("resultMap"));
        //2.配置select｜Insert｜update｜delete  暂时实现select
        buildStatementFromContext(element.elements("select"),
                element.elements("insert"),
                element.elements("update"),
                element.elements("delete"));
    }

    /**
     * <cache eviction="FIFO" flushInterval="600000" size="512" readOnly="true"/>
     */
    private void cacheElement(Element context) {
        if (context == null) {
            return;
        }
        // 基础配置信息
        String type = context.attributeValue("type", "PERPETUAL");
        Class<? extends Cache> typeClass = typeAliasRegistry.resolveAlias(type);
        // 缓存队列 FIFO
        String eviction = context.attributeValue("eviction", "FIFO");
        Class<? extends Cache> evictionClass = typeAliasRegistry.resolveAlias(eviction);
        Long flushInterval = Long.valueOf(context.attributeValue("flushInterval"));
        Integer size = Integer.valueOf(context.attributeValue("size"));
        boolean readWrite = !Boolean.parseBoolean(context.attributeValue("readOnly", "false"));
        boolean blocking = !Boolean.parseBoolean(context.attributeValue("blocking", "false"));

        // 解析额外属性信息；<property name="cacheFile" value="/tmp/xxx-cache.tmp"/>
        List<Element> elements = context.elements();
        Properties props = new Properties();
        for (Element element : elements) {
            props.setProperty(element.attributeValue("name"), element.attributeValue("value"));
        }
        // 构建缓存
        mapperBuilderAssistant.useNewCache(typeClass, evictionClass, flushInterval, size, readWrite, blocking, props);
    }

    private void resultMapElements(List<Element> list){
        for(Element element : list){
            try{
                resultMapElement(element, Collections.emptyList());
            }catch (Exception ignore){
            }
        }
    }
    //配置select｜Insert｜update｜delete
    @SafeVarargs
    private final void buildStatementFromContext(List<Element>... lists) {
        //遍历配置处理
        for(List<Element> list: lists) {
            for (Element element : list) {
                final XMLStatementBuilder statementParser = new XMLStatementBuilder(configuration, mapperBuilderAssistant, element);
                statementParser.parseStatementNode();
            }
        }
    }

    //解析Map元素
    private ResultMap resultMapElement(Element resultMapNode, List<ResultMapping> additionalResultMappings){
        //获得id属性值
        String id = resultMapNode.attributeValue("id");
        //获得type属性值
        String type = resultMapNode.attributeValue("type");
        //获得type类
        Class<?> typeClass = resolveClass(type);
        //设置一个集合用来装result标签中的column和property对应关系
        List<ResultMapping> resultMappings = new ArrayList<>();


        resultMappings.addAll(additionalResultMappings);


        List<Element> resultChildren = resultMapNode.elements();
        for(Element resultChild : resultChildren){
            List<ResultFlag> flags = new ArrayList<>();
            if("id".equals(resultChild.getName())){
                flags.add(ResultFlag.ID);
            }

            //构建ResultMapping
            resultMappings.add(buildResultMappingFromContext(resultChild,typeClass,flags));
        }

        //创建结果映射解析器
        ResultMapResolver resultMapResolver = new ResultMapResolver(mapperBuilderAssistant,id,typeClass, resultMappings);
        return resultMapResolver.resolve();
    }

    private ResultMapping buildResultMappingFromContext(Element context, Class<?> resultType, List<ResultFlag> flags){
        String property = context.attributeValue("property");
        String column = context.attributeValue("column");
        return mapperBuilderAssistant.buildResultMapping(resultType, property, column, flags);
    }
}
