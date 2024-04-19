package com.cps.mybatis.executor;

import com.cps.mybatis.executor.statement.StatementHandler;
import com.cps.mybatis.mapping.BoundSql;
import com.cps.mybatis.mapping.MappedStatement;
import com.cps.mybatis.session.Configuration;
import com.cps.mybatis.session.ResultHandler;
import com.cps.mybatis.session.defaults.RowBounds;
import com.cps.mybatis.transaction.Transaction;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

/**
 * @author cps
 * @description: 简单执行器实现
 * @date 2024/1/19 13:50
 * @OtherDescription: Other things
 */
public class SimpleExecutor extends BaseExecutor{

    public SimpleExecutor(Configuration configuration, Transaction transaction) {
        super(configuration, transaction);
    }

    @Override
    protected <E> List<E> doQuery(MappedStatement ms, Object parameter, ResultHandler resultHandler, RowBounds rowBounds, BoundSql boundSql) {
        try {
            Configuration configuration = ms.getConfiguration();//获取配置类
            //创建语句处理器
            StatementHandler handler = configuration.newStatementHandler(this, ms, parameter, resultHandler, rowBounds, boundSql);
            //通过事务获取链接
            Connection connection = transaction.getConnection();
            //通过语句处理器准备语句
            Statement stmt = handler.prepare(connection);
            //参数化语句
            handler.parameterize(stmt);
            return handler.query(stmt, resultHandler);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
