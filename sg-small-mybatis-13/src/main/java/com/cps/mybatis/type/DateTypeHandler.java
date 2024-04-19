package com.cps.mybatis.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * @author cps
 * @description: 日期类型处理器
 * @date 2024/2/19 11:22
 * @OtherDescription: Other things
 */
public class DateTypeHandler extends BaseTypeHandler<Date>{

        @Override
        protected void setNotNUllParameter(PreparedStatement ps, int i, Date parameter, JdbcType jdbcType) throws SQLException {
            ps.setTimestamp(i, new Timestamp((parameter).getTime()));
        }

        @Override
        protected Date getNullableResult(ResultSet rs, String columnName) throws SQLException {
            Timestamp sqlTimestamp = rs.getTimestamp(columnName);
            if (sqlTimestamp != null) {
                return new Date(sqlTimestamp.getTime());
            }
            return null;
        }

}
