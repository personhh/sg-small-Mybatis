package com.cps.mybatis.session.defaults;

import com.cps.mybatis.mapping.BoundSql;
import com.cps.mybatis.mapping.Environment;
import com.cps.mybatis.mapping.MappedStatement;
import com.cps.mybatis.session.Configuration;
import com.cps.mybatis.session.SqlSession;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DefaultSqlSession implements SqlSession {

    /**
     * 映射器注册机
     */
    private Configuration configuration;

    public DefaultSqlSession(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public <T> T selectOne(String statement) {
        return (T) ("你被代理了！" + statement);
    }

    //查询一个
    @Override
    public <T> T selectOne(String statement, Object parameter) {
        MappedStatement mappedStatement = configuration.getMappedStatement(statement);
        Environment environment = configuration.getEnvironment();

        try {
            //jdbc连接
            Connection connection = environment.getDataSource().getConnection();
            BoundSql boundSql = mappedStatement.getBoundSql();
            PreparedStatement preparedStatement = connection.prepareStatement(boundSql.getSql());
            preparedStatement.setLong(1,Long.parseLong(((Object[]) parameter)[0].toString()));
            ResultSet resultSet = preparedStatement.executeQuery();

            List<T> objList = resultSet20bj(resultSet, Class.forName(boundSql.getResultType()));
            return objList.get(0);
        } catch (Exception e) {
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
                T obj = (T) clazz.newInstance();//获取返回值类型的对象
                for (int i = 1; i <= columnCount; i++) {
                    Object value = resultSet.getObject(i);//获得返回结果1、10001、1_04、cps、2022-04-13 13:00:00.0
                    String columnName = metaData.getColumnName(i);//按照属性号获得返回结果属性 ，比如：id、parameterType等
                    String setMethod = "set" + columnName.substring(0, 1).toUpperCase() + columnName.substring(1);//set + I + d = setId
                    Method method;
                    if (value instanceof Timestamp) {//判断从数据库返回的属性是不是时间类型（Timestamp），为什么不是data，
                        // 因为数据库表中的属性类型是TimeStamp，而实体类中的时间用的是data，所以需要转换一下
                        method = clazz.getMethod(setMethod, Date.class);//是的话传入方法的返回参数是Data，也就是例如：private Data setCreateTime(){}
                    } else {
                        method = clazz.getMethod(setMethod, value.getClass());
                    }
                    method.invoke(obj, value);
                }
                list.add(obj);//把这个结果对象
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public <T> T getMapper(Class<T> type) {
        return configuration.getMapper(type,this);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }
}
