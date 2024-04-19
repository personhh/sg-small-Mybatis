package com.cps.mybatis.builder.xml;

import com.cps.mybatis.builder.BaseBuilder;
import com.cps.mybatis.datasource.DataSourceFactory;
import com.cps.mybatis.io.Resources;
import com.cps.mybatis.mapping.Environment;
import com.cps.mybatis.session.Configuration;
import com.cps.mybatis.transaction.TransactionFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Properties;

public class XMLConfigBuilder extends BaseBuilder {

    private Element root;

    public XMLConfigBuilder(Reader reader){
        //1.调用父类Configuration
        super(new Configuration());
        //2.dom4j处理xml
        SAXReader saxReader = new SAXReader();
        try{
            Document document = saxReader.read(new InputSource(reader));
            root = document.getRootElement();
        }catch (DocumentException e){
            e.printStackTrace();
        }
    }

    /**
     * 解析配置：类型别名、插件、对象工厂、对象包装工厂、设置、环境、类型转换、映射器
     * */
    public Configuration parse(){
        try{
            //环境
            environmentsElement(root.element("environments"));
            //解析映射器
            mapperElement(root.element("mappers"));
        }catch (Exception e){
            throw new RuntimeException("Error paring SQL Mapper Configuration. Cause: " + e , e);
        }
        return configuration;
    }

    private void mapperElement(Element mappers) throws  Exception{
        List<Element> mapperList = mappers.elements("mapper");
        for(Element e : mapperList){
            String resource = e.attributeValue("resource");
            String mapperClass = e.attributeValue("class");
            //xml解析
            if(resource != null && mapperClass == null){
                InputStream inputStream = Resources.getResourceAsStream(resource);
                //在for循环里每个mapper都重新new一个XMLMapperBuilder，来解析
                XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream,configuration,resource);
                mapperParser.parse();
            }
            //Annotation 注解解析
            else if(resource == null && mapperClass != null){
                Class<?> mapperInterface = Resources.classForName(mapperClass);
                configuration.addMapper(mapperInterface);
            }
        }
    }


    /*<environments default="development">
        <environment id="development">
            <transactionManager type="JDBC"/>
            <dataSource type="DRUID">
                <property name="driver" value="com.mysql.jdbc.Driver"/>
                <property name="url" value="jdbc:mysql://127.0.0.1:3306/mybatis?useUnicode=true"/>
                <property name="username" value="root"/>
                <property name="password" value="52ni1314"/>
            </dataSource>
        </environment>
    </environments>
*/
    private void environmentsElement(Element context)throws Exception{
        String environment = context.attributeValue("default");
        List<Element> environmentList = context.elements("environment");
        for(Element e : environmentList){
            String id = e.attributeValue("id");
            if(environment.equals(id)){
                //事务管理器
                TransactionFactory txFactory = (TransactionFactory) typeAliasRegistry.resolveAlias(e.element("transactionManager").attributeValue("type")).newInstance();

                //数据源
                Element dataSourceElement = e.element("dataSource");
                DataSourceFactory dataSourceFactory = (DataSourceFactory) typeAliasRegistry.resolveAlias(dataSourceElement.attributeValue("type")).newInstance();
                List<Element> propertyList = dataSourceElement.elements("property");
                Properties props = new Properties();
                for(Element property : propertyList){
                    props.setProperty(property.attributeValue("name"), property.attributeValue("value"));
                }
                dataSourceFactory.setProperties(props);
                DataSource dataSource = dataSourceFactory.getDataSource();

                //构建环境
                Environment.Builder environmentBuilder = new Environment.Builder(id)
                        .transactionFactory(txFactory)
                        .dataSource(dataSource);
                configuration.setEnvironment(environmentBuilder.build());
            }
        }
    }

}
