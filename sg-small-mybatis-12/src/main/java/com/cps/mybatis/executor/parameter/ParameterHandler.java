package com.cps.mybatis.executor.parameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author cps
 * @description: 参数处理器
 * @date 2024/1/26 12:16
 * @OtherDescription: Other things
 */
public interface ParameterHandler {

    //获取参数
    Object getParameterObject();

    //设置参数
    void setParameters(PreparedStatement ps) throws SQLException;
}
