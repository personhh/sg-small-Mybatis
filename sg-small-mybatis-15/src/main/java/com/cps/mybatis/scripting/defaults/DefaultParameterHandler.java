package com.cps.mybatis.scripting.defaults;

import com.alibaba.fastjson.JSON;
import com.cps.mybatis.executor.parameter.ParameterHandler;
import com.cps.mybatis.mapping.BoundSql;
import com.cps.mybatis.mapping.MappedStatement;
import com.cps.mybatis.mapping.ParameterMapping;
import com.cps.mybatis.reflection.MetaObject;
import com.cps.mybatis.session.Configuration;
import com.cps.mybatis.type.JdbcType;
import com.cps.mybatis.type.TypeHandler;
import com.cps.mybatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * @author cps
 * @description: 默认参数处理器
 * @date 2024/1/26 12:15
 * @OtherDescription: Other things
 */
public class DefaultParameterHandler implements ParameterHandler {

    private Logger logger = LoggerFactory.getLogger(DefaultParameterHandler.class);

    private final TypeHandlerRegistry typeHandlerRegistry;

    private final MappedStatement mappedStatement;
    private final Object parameterObject;
    private BoundSql boundSql;
    private Configuration configuration;

    public DefaultParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        this.mappedStatement = mappedStatement;
        this.configuration = mappedStatement.getConfiguration();
        this.typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
        this.parameterObject = parameterObject;
        this.boundSql = boundSql;
    }


    @Override
    public Object getParameterObject() {
        return parameterObject;
    }

    @Override
    public void setParameters(PreparedStatement ps) throws SQLException {
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if(null != parameterMappings){
            for(int i = 0; i < parameterMappings.size(); i++){
                ParameterMapping parameterMapping = parameterMappings.get(i);
                String propertyName = parameterMapping.getProperty();
                Object value;
                if(typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())){
                    value = parameterObject;
                }else{
                    //通过MetaObject.getValue 反射取得值设进去
                    MetaObject metaObject = configuration.newMetaObject(parameterObject);
                    value = metaObject.getValue(propertyName);
                }

                JdbcType jdbcType = parameterMapping.getJdbcType();

                //设置参数
                logger.info("根据每个ParameterMapping中的TypeHandler设置对应的参数信息 value：{}", JSON.toJSONString(value));
                TypeHandler typeHandler = parameterMapping.getTypeHandler();
                typeHandler.setParameter(ps,i + 1, value, jdbcType);
            }
        }
    }
}
