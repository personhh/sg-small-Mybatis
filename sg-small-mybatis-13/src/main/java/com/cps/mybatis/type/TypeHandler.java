package com.cps.mybatis.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author cps
 * @description: 类型处理器
 * @date 2024/1/25 16:49
 * @OtherDescription: Other things
 */
public interface TypeHandler<T> {
    /**
     * 设置参数
     * */
    void setParameter(PreparedStatement ps, int i,T parameter, JdbcType jdbcType) throws SQLException;


    //获取结果
    T getResult(ResultSet rs, String columnNames) throws SQLException;
}
