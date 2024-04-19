package com.cps.mybatis.builder;

import com.cps.mybatis.mapping.ParameterMapping;
import com.cps.mybatis.mapping.SqlSource;
import com.cps.mybatis.parse.GenericTokenParser;
import com.cps.mybatis.parse.TokenHandler;
import com.cps.mybatis.reflection.MetaClass;
import com.cps.mybatis.reflection.MetaObject;
import com.cps.mybatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author cps
 * @description: SQL 源码构建器
 * @date 2024/1/25 15:20
 * @OtherDescription: Other things
 */
public class SqlSourceBuilder extends BaseBuilder {

    private static Logger logger = LoggerFactory.getLogger(SqlSourceBuilder.class);
    private static final String parameterProperties = "javaType,jdbcType,mode,numericScale,resultMap,typeHandler,jdbcTypeName";

    public SqlSourceBuilder(Configuration configuration) {
        super(configuration);
    }

    public SqlSource parse(String originalSql, Class<?> parameterType, Map<String, Object> additionParameters){
        ParameterMappingTokenHandler handler = new ParameterMappingTokenHandler(configuration, parameterType, additionParameters);
        GenericTokenParser parser = new GenericTokenParser("#{","}", handler);
        String sql = parser.parse(originalSql);
        //返回静态SQL
        return new StaticSqlSource(configuration,sql,handler.getParameterMappings());
    }

    private static class ParameterMappingTokenHandler extends BaseBuilder implements TokenHandler {

        private List<ParameterMapping> parameterMappings = new ArrayList<>();
        private Class<?> parameterType;
        private MetaObject metaParameters;

        public ParameterMappingTokenHandler(Configuration configuration, Class<?> parameterType, Map<String, Object> additionalParameters) {
            super(configuration);
            this.parameterType = parameterType;
            this.metaParameters = configuration.newMetaObject(additionalParameters);
        }

        public List<ParameterMapping> getParameterMappings(){
            return parameterMappings;
        }

        @Override
        public String handleToken(String content) {
            parameterMappings.add(buildParameterMapping(content));
            return "?";
        }

        //构建参数映射
        private ParameterMapping buildParameterMapping(String content){
            //先解析参数映射，就是转化成一个HashMap
            Map<String, String> propertiesMap = new ParameterExpression(content);
            String property = propertiesMap.get("property");
            Class<?> propertyType;
            if(typeHandlerRegistry.hasTypeHandler(parameterType)){
                propertyType = parameterType;
            }else if(property != null){
                MetaClass metaClass = MetaClass.forClass(parameterType);
                if(metaClass.hasGetter(property)){
                    propertyType = metaClass.getGetterType(property);
                }else{
                    propertyType = Object.class;
                }
            }else{
                propertyType = Object.class;
            }
            logger.info("构建参数映射 property：{} propertyType：{}", property, propertyType);
            ParameterMapping.Builder builder = new ParameterMapping.Builder(configuration, property, propertyType);
            return builder.build();
        }
    }
}
