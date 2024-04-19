package com.cps.mybatis.executor.resultset;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;


/**
 * @author cps
 * @description: 结果集处理器接口
 * @date 2024/1/19 14:35
 * @OtherDescription: Other things
 */
public interface ResultSetHandler {

    <E> List<E> handleResultSets(Statement stmt) throws SQLException;
}
