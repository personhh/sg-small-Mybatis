package com.cps.mybatis.executor.resultset;

import com.cps.mybatis.executor.Executor;
import com.cps.mybatis.executor.result.DefaultResultContext;
import com.cps.mybatis.executor.result.DefaultResultHandler;
import com.cps.mybatis.mapping.BoundSql;
import com.cps.mybatis.mapping.MappedStatement;
import com.cps.mybatis.mapping.ResultMap;
import com.cps.mybatis.mapping.ResultMapping;
import com.cps.mybatis.reflection.MetaClass;
import com.cps.mybatis.reflection.MetaObject;
import com.cps.mybatis.reflection.factory.ObjectFactory;
import com.cps.mybatis.session.Configuration;
import com.cps.mybatis.session.ResultHandler;
import com.cps.mybatis.session.defaults.RowBounds;
import com.cps.mybatis.type.TypeHandler;
import com.cps.mybatis.type.TypeHandlerRegistry;

import javax.xml.transform.Result;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author cps
 * @description: 默认Map结果处理器
 * @date 2024/1/19 14:30
 * @OtherDescription: Other things
 */
public class DefaultResultSetHandler implements ResultSetHandler {
    private final BoundSql boundSql;

    private final MappedStatement mappedStatement;
    private final ObjectFactory objectFactory;
    private final Configuration configuration;
    private final TypeHandlerRegistry typeHandlerRegistry;
    private ResultHandler resultHandler;
    private final RowBounds rowBounds;


    public DefaultResultSetHandler(Executor executor, MappedStatement mappedStatement, ResultHandler resultHandler, RowBounds rowBounds,
                                   BoundSql boundSql) {
        this.configuration = mappedStatement.getConfiguration();
        this.boundSql = boundSql;
        this.mappedStatement = mappedStatement;
        this.objectFactory = configuration.getObjectFactory();
        this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        this.resultHandler = resultHandler;
        this.rowBounds = rowBounds;
    }

    @Override
    public List<Object> handleResultSets(Statement stmt) throws SQLException {
        final List<Object> multipleResults = new ArrayList<>();

        int resultSetCount = 0;
        //获得结果集对象
        ResultSetWrapper rsw = new ResultSetWrapper(stmt.getResultSet(), configuration);
        //
        List<ResultMap> resultMaps = mappedStatement.getResultMaps();
        while (rsw != null && resultMaps.size() > resultSetCount) {
            ResultMap resultMap = resultMaps.get(resultSetCount);
            handleResultSet(rsw, resultMap, multipleResults, null);
            rsw = getNextResultSet(stmt);
            resultSetCount++;
        }

        return multipleResults.size() == 1 ? (List<Object>) multipleResults.get(0) : multipleResults;

    }


    private ResultSetWrapper getNextResultSet(Statement stmt) throws SQLException {
        // Making this method tolerant of bad JDBC drivers
        try {
            if (stmt.getConnection().getMetaData().supportsMultipleResultSets()) {
                // Crazy Standard JDBC way of determining if there are more results
                if (!((!stmt.getMoreResults()) && (stmt.getUpdateCount() == -1))) {
                    ResultSet rs = stmt.getResultSet();
                    return rs != null ? new ResultSetWrapper(rs, configuration) : null;
                }
            }
        } catch (Exception ignore) {
            // Intentionally ignored.
        }
        return null;
    }

    private void handleResultSet(ResultSetWrapper rsw, ResultMap resultMap, List<Object> multipleResults, ResultMapping parentMapping) throws SQLException {
        if (resultHandler == null) {
            // 1. 新创建结果处理器
            DefaultResultHandler defaultResultHandler = new DefaultResultHandler(objectFactory);
            // 2. 封装数据
            handleRowValuesForSimpleResultMap(rsw, resultMap, defaultResultHandler, rowBounds, null);
            // 3. 保存结果
            multipleResults.add(defaultResultHandler.getResultList());
        }
    }


    //封装数据
    private void handleRowValuesForSimpleResultMap(ResultSetWrapper rsw, ResultMap resultMap, ResultHandler resultHandler, RowBounds rowBounds, ResultMapping parentMapping) throws SQLException {
        DefaultResultContext resultContext = new DefaultResultContext();
        while (resultContext.getResultCount() < rowBounds.getLimit() && rsw.getResultSet().next()) {
            Object rowValue = getRowValue(rsw, resultMap);
            callResultHandler(resultHandler, resultContext, rowValue);
        }

    }

    private void callResultHandler(ResultHandler resultHandler, DefaultResultContext resultContext, Object rowValue) {
        resultContext.nextResultObject(rowValue);
        resultHandler.handleResult(resultContext);
    }


    private Object createResultObject(ResultSetWrapper rsw, ResultMap resultMap, String columnPrefix) throws SQLException {
        final List<Class<?>> constructorArgTypes = new ArrayList<>();
        final List<Object> constructorArgs = new ArrayList<>();
        return createResultObject(rsw, resultMap, constructorArgTypes, constructorArgs, columnPrefix);
    }

    //创建结果对象
    //对于这样的普通对象，只需要使用反射工具类就可以实例化对象了，不过这个时候属性信息还没有填充。其实和我们使用的 clazz.newInstance(); 也是一样的效果
    private Object createResultObject(ResultSetWrapper rsw, ResultMap resultMap, List<Class<?>> constructorArgTypes, List<Object> constructorArgs, String columnPrefix) throws SQLException{
        final Class<?> resultType = resultMap.getType();
        final MetaClass metaType = MetaClass.forClass(resultType);
        //判读返回类型是不是接口，或者这个反射对象是否有默认构造器
        if(resultType.isInterface() || metaType.hasDefaultConstructor()){
          //普通的Bean对象类型
          return objectFactory.create(resultType);
        }
        throw new RuntimeException("Do not know how to create an instance of " + resultType);
    }

    //获取一行的值
    private Object getRowValue(ResultSetWrapper rsw, ResultMap resultMap) throws SQLException {
        //根据返回类型，实例化对象
        Object resultObject = createResultObject(rsw, resultMap, null);
        if(resultObject != null && !typeHandlerRegistry.hasTypeHandler(resultMap.getType())){
            final MetaObject metaObject = configuration.newMetaObject(resultObject);
            applyAutomaticMappings(rsw, resultMap, metaObject, null);
        }
        return  resultObject;
    }








    //根据ResultSet获取出对应的值填充到对象的属性
    private boolean applyAutomaticMappings(ResultSetWrapper rsw, ResultMap resultMap, MetaObject metaObject, String columnPrefix) throws SQLException {
        final List<String> unmappedColumnNames = rsw.getUnmappedColumnNames(resultMap, columnPrefix);
        boolean foundValues = false;
        for(String columnName : unmappedColumnNames){
            String propertyName = columnName;
            if(columnPrefix != null && !columnPrefix.isEmpty()){
                if(columnName.toUpperCase(Locale.ENGLISH).startsWith(columnPrefix)) {
                    propertyName = columnName.substring(columnPrefix.length());
                }else{
                    continue;
                }
            }
            final String property = metaObject.findProperty(propertyName, false);
            if(property != null && metaObject.hasSetter(property)){
                final Class<?> propertyType = metaObject.getSetterType(property);
                if(typeHandlerRegistry.hasTypeHandler(propertyType)){
                    final TypeHandler<?> typeHandler = rsw.getTypeHandler(propertyType, columnName);
                    //使用TypeHandler取得结果
                    final Object value = typeHandler.getResult(rsw.getResultSet(), columnName);
                    if(value != null){
                        foundValues = true;
                    }
                    if(value != null || !propertyType.isPrimitive()){
                        //通过反射工具类设置属性值
                        metaObject.setValue(property, value);
                    }
                }
            }
        }
        return foundValues;
    }


}
