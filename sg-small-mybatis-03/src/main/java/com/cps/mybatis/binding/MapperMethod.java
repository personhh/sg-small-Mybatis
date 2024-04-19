package com.cps.mybatis.binding;

import com.cps.mybatis.mapping.MappedStatement;
import com.cps.mybatis.mapping.SqlCommandType;
import com.cps.mybatis.session.Configuration;
import com.cps.mybatis.session.SqlSession;

import java.lang.reflect.Method;

//映射器方法
public class MapperMethod {

    private final SqlCommand command;

    public MapperMethod(Class<?> mapperInterface, Method method, Configuration configuration) {
        //获得映射器方法的sql指令
        this.command = new SqlCommand(configuration, mapperInterface, method);
    }

    public Object execute(SqlSession sqlSession, Object[] args) {
        Object result = null;
        switch (command.getType()) {
            case INSERT:
                break;
            case DELETE:
                break;
            case UPDATE:
                break;
            case SELECT:
                result = sqlSession.selectOne(command.getName(), args);
                break;
            default:
                throw new RuntimeException("Unknown execution method for: " + command.getName());
        }
        return result;
    }

    //SQL指令（name也就是id，指令类型）
    public static class SqlCommand {

        private final String name;
        private final SqlCommandType type;

        public SqlCommand(Configuration configuration, Class<?> mapperInterface, Method method) {
            //接口类方法的全类名
            String statementName = mapperInterface.getName() + "." + method.getName();
            //根据全类名返回映射语句
            MappedStatement ms = configuration.getMappedStatement(statementName);
            //获得语句的id（也就是全类名）
            name = ms.getId();
            //获得语句的指令类型
            type = ms.getSqlCommandType();
        }

        public String getName() {
            return name;
        }

        public SqlCommandType getType() {
            return type;
        }
    }
}
