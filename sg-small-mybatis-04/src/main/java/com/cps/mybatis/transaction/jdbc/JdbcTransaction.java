package com.cps.mybatis.transaction.jdbc;

import com.cps.mybatis.session.TransactionIsolationLevel;
import com.cps.mybatis.transaction.Transaction;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

//JDBC 事务，c

public class JdbcTransaction implements Transaction {

    //数据库连接类
    protected Connection connection;
    //数据源，放数据库必要配置的
    protected DataSource dataSource;
    //事务属性
    protected TransactionIsolationLevel level = TransactionIsolationLevel.NONE;
    //自动提交
    protected boolean autoCommit;


    public JdbcTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit) {
        this.dataSource = dataSource;
        this.level = level;
        this.autoCommit = autoCommit;
    }


    public JdbcTransaction(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Connection getConnection() throws Exception {
        connection = dataSource.getConnection();
        connection.setTransactionIsolation(level.getLevel());
        connection.setAutoCommit(autoCommit);
        return connection;
    }

    @Override
    public void commit() throws SQLException {
        if(connection != null && !connection.getAutoCommit()) {
            connection.commit();
        }
    }

    @Override
    public void rollback() throws SQLException {
        if(connection != null && !connection.getAutoCommit()){
            connection.rollback();
        }
    }

    @Override
    public void close() throws SQLException {
        if(connection != null && !connection.getAutoCommit()){
            connection.close();
        }
    }
}
