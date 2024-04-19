package com.cps.mybatis.builder.xml;

import com.cps.mybatis.builder.BaseBuilder;
import com.cps.mybatis.builder.MapperBuilderAssistant;
import com.cps.mybatis.executor.keygen.Jdbc3KeyGenerator;
import com.cps.mybatis.executor.keygen.KeyGenerator;
import com.cps.mybatis.executor.keygen.NoKeyGenerator;
import com.cps.mybatis.executor.keygen.SelectKeyGenerator;
import com.cps.mybatis.mapping.MappedStatement;
import com.cps.mybatis.mapping.SqlCommandType;
import com.cps.mybatis.mapping.SqlSource;
import com.cps.mybatis.scripting.LanguageDriver;
import com.cps.mybatis.session.Configuration;
import org.dom4j.Element;

import java.util.List;
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


    //解析语句(select|Insert|update|delete)
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


        //解析<selectKey>
        processSelectKeyNodes(id, parameterTypeClass, langDriver);

        //解析成SqlSource DynamicSqlSource/RawSqlSource
        SqlSource sqlSource = langDriver.createSqlSource(configuration, element, parameterTypeClass);

        // 属性标记【仅对 insert 有用】, MyBatis 会通过 getGeneratedKeys 或者通过 insert 语句的 selectKey 子元素设置它的值 step-14 新增
        String keyProperty = element.attributeValue("keyProperty");

        KeyGenerator keyGenerator = null;
        String keyStatementId = id + SelectKeyGenerator.SELECT_KEY_SUFFIX;
        keyStatementId = mapperBuilderAssistant.applyCurrentNamespace(keyStatementId, true);

        if (configuration.hasKeyGenerator(keyStatementId)) {
            keyGenerator = configuration.getKeyGenerator(keyStatementId);
        } else {
            keyGenerator = configuration.isUseGeneratedKeys() && SqlCommandType.INSERT.equals(sqlCommandType) ? new Jdbc3KeyGenerator() : new NoKeyGenerator();
        }



        //调用助手类（本节新添加，便于统一处理参数的包装）
        mapperBuilderAssistant.addMappedStatement(id,sqlSource,sqlCommandType,parameterTypeClass,resultMap,resultTypeClass,keyGenerator,keyProperty,langDriver);

    }


    private void processSelectKeyNodes(String id, Class<?> parameterTypeClass, LanguageDriver langDriver) {
        List<Element> selectKeyNodes = element.elements("selectKey");
        parseSelectKeyNodes(id, selectKeyNodes, parameterTypeClass, langDriver);
    }

    private void parseSelectKeyNodes(String parentId, List<Element> list, Class<?> parameterTypeClass, LanguageDriver languageDriver) {
        for (Element nodeToHandle : list) {
            String id = parentId + SelectKeyGenerator.SELECT_KEY_SUFFIX;
            parseSelectKeyNode(id, nodeToHandle, parameterTypeClass, languageDriver);
        }
    }

    /**
     * <selectKey keyProperty="id" order="AFTER" resultType="long">
     * SELECT LAST_INSERT_ID()
     * </selectKey>
     */
    private void parseSelectKeyNode(String id, Element nodeToHandle, Class<?> parameterTypeClass, LanguageDriver langDriver) {
        String resultType = nodeToHandle.attributeValue("resultType");
        Class<?> resultTypeClass = resolveClass(resultType);
        boolean executeBefore = "BEFORE".equals(nodeToHandle.attributeValue("order", "AFTER"));
        String keyProperty = nodeToHandle.attributeValue("keyProperty");

        // default
        String resultMap = null;
        KeyGenerator keyGenerator = new NoKeyGenerator();

        // 解析成SqlSource，DynamicSqlSource/RawSqlSource
        SqlSource sqlSource = langDriver.createSqlSource(configuration, nodeToHandle, parameterTypeClass);
        SqlCommandType sqlCommandType = SqlCommandType.SELECT;

        // 调用助手类
        mapperBuilderAssistant.addMappedStatement(id,
                sqlSource,
                sqlCommandType,
                parameterTypeClass,
                resultMap,
                resultTypeClass,
                keyGenerator,
                keyProperty,
                langDriver);

        // 给id加上namespace前缀
        id = mapperBuilderAssistant.applyCurrentNamespace(id, false);

        // 存放键值生成器配置
        MappedStatement keyStatement = configuration.getMappedStatement(id);
        configuration.addKeyGenerator(id, new SelectKeyGenerator(keyStatement, executeBefore));
    }

}
