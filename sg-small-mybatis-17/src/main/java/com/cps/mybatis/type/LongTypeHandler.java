package com.cps.mybatis.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author cps
 * @description: Long类型处理器
 * @date 2024/1/25 17:07
 * @OtherDescription: Other things
 */
public class LongTypeHandler extends BaseTypeHandler<Long>{


    @Override
    protected void setNotNUllParameter(PreparedStatement ps, int i, Long parameter, JdbcType jdbcType) throws SQLException {
        ps.setLong(i,parameter);
    }


    @Override
    protected Long getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getLong(columnName);
    }

    @Override
    public Long getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getLong(columnIndex);
    }
}
