package com.cps.mybatis.executor.keygen;

import com.cps.mybatis.executor.Executor;
import com.cps.mybatis.mapping.MappedStatement;

import java.sql.Statement;

/**
 * @author cps
 * @description: 不用键值生成器
 * @date 2024/2/20 14:05
 * @OtherDescription: Other things
 */
public class NoKeyGenerator implements KeyGenerator{
    @Override
    public void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {

    }

    @Override
    public void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {

    }
}
