package com.cps.mybatis.mapping;

import com.cps.mybatis.session.Configuration;
import com.cps.mybatis.type.JdbcType;
import com.cps.mybatis.type.TypeHandler;
import com.cps.mybatis.type.TypeHandlerRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cps
 * @description: 结果映射
 * @date 2024/1/26 20:37
 * @OtherDescription: Other things
 */
public class ResultMapping {

    private Configuration configuration;
    private Class<?> javaType;
    private JdbcType jdbcType;
    private String property;
    private String column;
    private TypeHandler<?> typeHandler;
    private List<ResultFlag> flags;

    public ResultMapping() {
    }

    public static class Builder{
        private ResultMapping resultMapping = new ResultMapping();

        public Builder(Configuration configuration, String property, String column, Class<?> javaType){
            resultMapping.configuration = configuration;
            resultMapping.property = property;
            resultMapping.column = column;
            resultMapping.javaType = javaType;
            resultMapping.flags = new ArrayList<>();
        }


        public Builder typeHandler(TypeHandler<?> typeHandler) {
            resultMapping.typeHandler = typeHandler;
            return this;
        }

        public Builder flags(List<ResultFlag> flags) {
            resultMapping.flags = flags;
            return this;
        }

        public ResultMapping build() {
            resolveTypeHandler();
            return resultMapping;
        }

        private void resolveTypeHandler() {
            if (resultMapping.typeHandler == null && resultMapping.javaType != null) {
                Configuration configuration = resultMapping.configuration;
                TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
                resultMapping.typeHandler = typeHandlerRegistry.getTypeHandler(resultMapping.javaType, null);
            }
        }

    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public String getProperty() {
        return property;
    }

    public String getColumn() {
        return column;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public TypeHandler<?> getTypeHandler() {
        return typeHandler;
    }

    public List<ResultFlag> getFlags() {
        return flags;
    }

}
