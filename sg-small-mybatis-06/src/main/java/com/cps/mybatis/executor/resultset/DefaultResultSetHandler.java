package com.cps.mybatis.executor.resultset;

import com.cps.mybatis.executor.Executor;
import com.cps.mybatis.mapping.BoundSql;
import com.cps.mybatis.mapping.MappedStatement;
import com.cps.mybatis.session.ResultHandler;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author cps
 * @description: 默认Map结果处理器
 * @date 2024/1/19 14:30
 * @OtherDescription: Other things
 */
public class DefaultResultSetHandler implements ResultSetHandler {
    private final BoundSql boundSql;

    public DefaultResultSetHandler(Executor executor, MappedStatement mappedStatement, BoundSql boundSql) {
        this.boundSql = boundSql;
    }

    @Override
    public <E> List<E> handleResultSets(Statement stmt) throws SQLException {
        ResultSet resultSet = stmt.getResultSet();
        try{
            return resultSet20bj(resultSet,Class.forName(boundSql.getResultType()));
        }catch (ClassNotFoundException e){
            e.printStackTrace();
            return null;
        }
    }

    private <T> List<T> resultSet20bj(ResultSet resultSet, Class<?> clazz){
        List<T> list = new ArrayList<>();
        try{
            /**通过MetaData来获取具体的表的相关信息。可以查询数据库中的有哪些表，表有哪些字段，字段的属性等等。
             * MetaData中通过一系列getXXX函数，将这些信息存放到ResultSet里面，然后返回给用户。*/
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();//获得列数
            //每次遍历行值
            while(resultSet.next()){
                T obj = (T) clazz.newInstance();
                for (int i = 1; i <= columnCount; i++) {
                    Object value = resultSet.getObject(i);//获得返回结果1、10001、1_04、cps、2022-04-13 13:00:00.0
                    String columnName = metaData.getColumnName(i);//获得返回结果属性
                    String setMethod = "set" + columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
                    Method method;
                    if (value instanceof Timestamp) {
                        method = clazz.getMethod(setMethod, Date.class);
                    } else {
                        method = clazz.getMethod(setMethod, value.getClass());
                    }
                    method.invoke(obj, value);
                }
                list.add(obj);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }
}
