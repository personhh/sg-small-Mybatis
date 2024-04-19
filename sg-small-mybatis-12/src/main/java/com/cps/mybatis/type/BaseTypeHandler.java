package com.cps.mybatis.type;

import com.cps.mybatis.session.Configuration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author cps
 * @description: TODO
 * @date 2024/1/25 17:04
 * @OtherDescription: Other things
 */
public abstract class BaseTypeHandler <T> implements TypeHandler<T>{
    protected Configuration configuration;

   public void setConfiguration(Configuration configuration){
       this.configuration = configuration;
   }


    @Override
    public void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        //定义抽象方法，有子类实现不同类型的属性设置
        setNotNUllParameter(ps,i,parameter,jdbcType);
    }

    protected abstract void setNotNUllParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException;


    @Override
    public T getResult(ResultSet rs, String columnNames) throws SQLException {
        return getNullableResult(rs, columnNames);
    }

    protected abstract T getNullableResult(ResultSet re, String columnName) throws SQLException;
}
