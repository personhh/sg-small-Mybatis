package com.cps.mybatis.builder.xml;

import com.cps.mybatis.builder.BaseBuilder;
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
    private String currentNamespace;

    public XMLMapperBuilder(InputStream inputStream,Configuration configuration, String resource) throws DocumentException {
        this(new SAXReader().read(inputStream), configuration, resource);
    }

    private XMLMapperBuilder(Document document, Configuration configuration, String resource){
        super(configuration);
        this.element = document.getRootElement();
        this.resource = resource;
    }

    /**
     * 解析
     * <mapper namespace="com.cps.mybatis.Test.Dao.IActivityDao">
     *
     *     <select id="queryUserInfoById" parameterType="java.lang.Long" resultType="com.cps.mybatis.Test.Po.Activity">
     *         SELECT id, userId,userHead,userName,createTime
     *         FROM USER
     *         where id = #{id}
     *     </select>
     *
     * </mapper>
     * */
    public void parse() throws Exception{
        //判断资源有没有加载过，防止重复加载
        if(!configuration.isResourceLoaded(resource)){
            configurationElement(element);
            //标记一下，已经加载过啦
            configuration.addLoadedResource(resource);
            //绑定映射器到namespace（namespace就是接口的全路径com.cps.mybatis.Test.Dao.xxxDao,然后通过classForName的方法加载出类并放入到绑定映射器）
            configuration.addMapper(Resources.classForName(currentNamespace));
        }
    }

    private void configurationElement(Element element) {
        //1.配置namespace，检查为不为空
        currentNamespace = element.attributeValue("namespace");
        if(currentNamespace.equals("")){
            throw new RuntimeException("Mapper's namespace cannot be empty");
        }

        //2.配置select｜insert｜update｜delete  暂时实现select
        buildStatementFromContext(element.elements("select"));
    }

    //配置select｜insert｜update｜delete
    private void buildStatementFromContext(List<Element> list) {
        //遍历配置处理
        for(Element element : list){
            final XMLStatementBuilder statementParser = new XMLStatementBuilder(configuration, element, currentNamespace);
            statementParser.parseStatementNode();
        }
    }
}
