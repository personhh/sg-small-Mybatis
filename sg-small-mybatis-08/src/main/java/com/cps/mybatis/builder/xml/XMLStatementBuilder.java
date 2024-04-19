package com.cps.mybatis.builder.xml;

import com.cps.mybatis.builder.BaseBuilder;
import com.cps.mybatis.mapping.MappedStatement;
import com.cps.mybatis.mapping.SqlCommandType;
import com.cps.mybatis.mapping.SqlSource;
import com.cps.mybatis.scripting.LanguageDriver;
import com.cps.mybatis.session.Configuration;
import org.dom4j.Element;

import java.util.Locale;

/**
 * @author cps
 * @description: TODO
 * @date 2024/1/25 10:47
 * @OtherDescription: Other things
 */
public class XMLStatementBuilder extends BaseBuilder {

    private String currentNamespace;
    private Element element;

    public XMLStatementBuilder(Configuration configuration, Element element, String currentNamespace) {
        super(configuration);
        this.currentNamespace = currentNamespace;
        this.element = element;
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
        //获得结果类型
        String resultType = element.attributeValue("resultType");
        Class<?> resultTypeClass = resolveAlias(resultType);
        //获取命令类型
        String nodeName = element.getName();
        SqlCommandType sqlCommandType = SqlCommandType.valueOf(nodeName.toUpperCase(Locale.ENGLISH));

        //获取默认语言驱动器
        Class<?> langClass = configuration.getLanguageRegistry().getDefaultDriverClass();
        LanguageDriver langDriver = configuration.getLanguageRegistry().getDriver(langClass);

        //使用默认语言驱动器来解析sql，此时sqlSource就是已经把#{}换成？
        SqlSource sqlSource = langDriver.createSqlSource(configuration, element, parameterTypeClass);

        MappedStatement mappedStatement = new MappedStatement.Builder(configuration, currentNamespace + "." + id, sqlCommandType, sqlSource, resultTypeClass).build();

        //添加解析 sql
        configuration.addMappedStatement(mappedStatement);

    }
}
