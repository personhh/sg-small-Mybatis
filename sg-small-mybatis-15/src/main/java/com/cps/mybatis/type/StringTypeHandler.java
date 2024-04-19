package com.cps.mybatis.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author cps
 * @description: TODO
 * @date 2024/1/25 17:12
 * @OtherDescription: Other things
 */
public class StringTypeHandler extends BaseTypeHandler<String>{
    @Override
    protected void setNotNUllParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i,parameter);
    }

    @Override
    protected String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getString(columnName);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getString(columnIndex);
    }
}
