package com.cps.mybatis.executor.statement;

import com.cps.mybatis.mapping.BoundSql;
import com.cps.mybatis.session.ResultHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author cps
 * @description: 语句处理器接口
 * @date 2024/1/19 14:19
 * @OtherDescription: 准备语句、参数化传递、执行SQL、封装结果
 * (语句处理器的核心包括了；准备语句、参数化传递参数、执行查询的操作，这里对应的 Mybatis 源码中还包括了 update、批处理、获取参数处理器等。)
 */
public interface StatementHandler {

    /** 准备语句 */
    Statement prepare(Connection connection) throws SQLException;

    /** 参数化 */
    void parameterize(Statement statement) throws SQLException;

    /** 执行查询 */
    <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException;

    /**执行增加删除更新操作*/
    int update(Statement stmt) throws SQLException;

    /** 获取绑定SQL */
    BoundSql getBoundSql();

}
