package com.cps.mybatis.builder.xml;

import com.cps.mybatis.builder.BaseBuilder;
import com.cps.mybatis.builder.MapperBuilderAssistant;
import com.cps.mybatis.mapping.MappedStatement;
import com.cps.mybatis.mapping.SqlCommandType;
import com.cps.mybatis.mapping.SqlSource;
import com.cps.mybatis.scripting.LanguageDriver;
import com.cps.mybatis.session.Configuration;
import org.dom4j.Element;

import java.util.Locale;

/**
 * @author cps
 * @description: xml语句构建器
 * @date 2024/1/25 10:47
 * @OtherDescription: Other things
 */
public class XMLStatementBuilder extends BaseBuilder {

    private MapperBuilderAssistant mapperBuilderAssistant;
    private Element element;

    public XMLStatementBuilder(Configuration configuration, MapperBuilderAssistant mapperBuilderAssistant, Element element) {
        super(configuration);
        this.element = element;
        this.mapperBuilderAssistant = mapperBuilderAssistant;
    }


    //解析语句(select|insert|update|delete)
    //<select
    //  id="selectPerson"
    //  parameterType="int"
    //  parameterMap="deprecated"
    //  resultType="hashmap"
    //  resultMap="personResultMap"
    //  flushCache="false"
    //  useCache="true"
    //  timeout="10000"
    //  fetchSize="256"
    //  statementType="PREPARED"
    //  resultSetType="FORWARD_ONLY">
    //  SELECT * FROM PERSON WHERE ID = #{id}
    //</select>
    public void parseStatementNode() {
        //id
        String id = element.attributeValue("id");
        //参数类型 parameterType
        String parameterType = element.attributeValue("parameterType");
        //获得参数类型
        Class<?> parameterTypeClass = resolveAlias(parameterType);
        //外部应用resultMap
        String resultMap = element.attributeValue("resultMap");
        //获得结果类型
        String resultType = element.attributeValue("resultType");
        Class<?> resultTypeClass = resolveAlias(resultType);
        //获取命令类型
        String nodeName = element.getName();
        SqlCommandType sqlCommandType = SqlCommandType.valueOf(nodeName.toUpperCase(Locale.ENGLISH));

        //获取默认语言驱动器
        Class<?> langClass = configuration.getLanguageRegistry().getDefaultDriverClass();
        LanguageDriver langDriver = configuration.getLanguageRegistry().getDriver(langClass);

        SqlSource sqlSource = langDriver.createSqlSource(configuration, element, parameterTypeClass);

        //调用助手类（本节新添加，便于统一处理参数的包装）
        mapperBuilderAssistant.addMappedStatement(id,sqlSource,sqlCommandType,parameterTypeClass,resultMap,resultTypeClass,langDriver);

    }
}
