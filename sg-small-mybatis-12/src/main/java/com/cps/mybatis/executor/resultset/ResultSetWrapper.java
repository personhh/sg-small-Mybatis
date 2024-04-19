package com.cps.mybatis.executor.resultset;

import com.cps.mybatis.mapping.ResultMap;
import com.cps.mybatis.session.Configuration;
import com.cps.mybatis.type.JdbcType;
import com.cps.mybatis.type.TypeHandler;
import com.cps.mybatis.type.TypeHandlerRegistry;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * @author cps
 * @description: 结果集包装器
 * @date 2024/1/28 11:05
 * @OtherDescription: Other things
 */
public class ResultSetWrapper {
    private final ResultSet resultSet;
    private final TypeHandlerRegistry typeHandlerRegistry;
    private final List<String> columNames = new ArrayList<>();
    private final List<String> classNames = new ArrayList<>();
    private final List<JdbcType> jdbcTypes = new ArrayList<>();
    private final Map<String, Map<Class<?>, TypeHandler<?>>> typeHandlerMap = new HashMap<>();
    private Map<String, List<String>> mappedColumnNamesMap = new HashMap<>();
    private Map<String, List<String>> unMappedColumnNamesMap = new HashMap<>();

    public ResultSet getResultSet() {
        return resultSet;
    }

    public List<String> getColumNames() {
        return this.columNames;
    }

    public List<String> getClassNames() {
        return Collections.unmodifiableList(classNames);
    }

    //包装结果集
    public ResultSetWrapper(ResultSet rs, Configuration configuration) throws SQLException {
        super();
        this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        this.resultSet = rs;
        //jdbc中通过这个方法来获取具体的表的相关信息，可以查询数据库中的有哪些表，表有哪些字段，字段的属性等
        final ResultSetMetaData metaData = rs.getMetaData();
        //获得表中的数据列数
        final int columnCount = metaData.getColumnCount();
        for(int i = 1; i <= columnCount; i++){
            //添加列属性
            columNames.add(metaData.getColumnLabel(i));
            //jdbc类型
            jdbcTypes.add(JdbcType.forCode(metaData.getColumnType(i)));
            //添加实例的java类的完全限定名称
            classNames.add(metaData.getColumnClassName(i));
        }
    }

    //获得类型处理器
    public TypeHandler<?> getTypeHandler(Class<?> propertyType, String columnName){
        TypeHandler<?> handler = null;
        Map<Class<?>, TypeHandler<?>> columnHandlers = typeHandlerMap.get(columnName);
        if(columnHandlers == null){
            columnHandlers = new HashMap<>();
            typeHandlerMap.put(columnName, columnHandlers);
        }else{
            handler = columnHandlers.get(propertyType);
        }
        if(handler == null){
            handler = typeHandlerRegistry.getTypeHandler(propertyType, null);
            columnHandlers.put(propertyType,handler);
        }
        return handler;
    }


    public List<String> getMappedColumnNames(ResultMap resultMap, String columnPrefix){
        List<String> mappedColumnNames = mappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
        if (mappedColumnNames == null) {
            loadMappedAndUnmappedColumnNames(resultMap, columnPrefix);
            mappedColumnNames = mappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
        }
        return mappedColumnNames;
    }
    public List<String> getUnmappedColumnNames(ResultMap resultMap, String columnPrefix) {
        List<String> unMappedColumnNames = unMappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
        if(unMappedColumnNames == null){
            loadMappedAndUnmappedColumnNames(resultMap,columnPrefix);
            unMappedColumnNames = unMappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
        }
        return unMappedColumnNames;
    }

    private String getMapKey(ResultMap resultMap, String colunmPrefix){
        return resultMap.getId() + ":" + colunmPrefix;
    }

    private void loadMappedAndUnmappedColumnNames(ResultMap resultMap, String columnPrefix) {
        List<String> mappedColumnNames = new ArrayList<String>();
        List<String> unmappedColumnNames = new ArrayList<String>();
        final String upperColumnPrefix = columnPrefix == null ? null : columnPrefix.toUpperCase(Locale.ENGLISH);
        final Set<String> mappedColumns = prependPrefixes(resultMap.getMappedColumns(), upperColumnPrefix);
        for (String columnName : columNames) {
            final String upperColumnName = columnName.toUpperCase(Locale.ENGLISH);
            if (mappedColumns.contains(upperColumnName)) {
                mappedColumnNames.add(upperColumnName);
            } else {
                unmappedColumnNames.add(columnName);
            }
        }

        mappedColumnNamesMap.put(getMapKey(resultMap, columnPrefix), mappedColumnNames);
        unMappedColumnNamesMap.put(getMapKey(resultMap, columnPrefix), unmappedColumnNames);
    }
    private Set<String> prependPrefixes(Set<String> columnNames, String prefix){
        if(columnNames == null || columnNames.isEmpty() || prefix == null || prefix.length() == 0){
            return columnNames;
        }
        final Set<String> prefixed = new HashSet<String>();
        for(String columnName : columnNames){
            prefixed.add(prefix + columnName);
        }
        return prefixed;
    }
}
