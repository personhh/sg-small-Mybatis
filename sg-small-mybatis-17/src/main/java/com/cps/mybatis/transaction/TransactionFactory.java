package com.cps.mybatis.transaction;

import com.cps.mybatis.session.TransactionIsolationLevel;

import javax.sql.DataSource;
import java.sql.Connection;

//事务工厂接口
public interface TransactionFactory {
    //根据Connection创建Transaction
    Transaction newTransaction(Connection conn);

    //根据数据源和事务隔离级别创建Transaction
    Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit);
}
