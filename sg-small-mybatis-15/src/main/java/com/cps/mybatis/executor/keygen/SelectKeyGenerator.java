package com.cps.mybatis.executor.keygen;

import com.cps.mybatis.executor.Executor;
import com.cps.mybatis.mapping.MappedStatement;
import com.cps.mybatis.reflection.MetaObject;
import com.cps.mybatis.session.Configuration;
import com.cps.mybatis.session.defaults.RowBounds;

import java.sql.Statement;
import java.util.List;

/**
 * @author cps
 * @description: 键值生成器
 * @date 2024/2/20 12:49
 * @OtherDescription: Other things
 */
public class SelectKeyGenerator implements KeyGenerator{
    public static final String SELECT_KEY_SUFFIX = "!selectKey";
    private boolean executeBefore;
    private MappedStatement keyStatement;

    public SelectKeyGenerator(MappedStatement keyStatement, boolean executeBefore) {
        this.executeBefore = executeBefore;
        this.keyStatement = keyStatement;
    }

    @Override
    public void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        if (executeBefore) {
            processGeneratedKeys(executor, ms, parameter);
        }
    }

    @Override
    public void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        if (!executeBefore) {
            processGeneratedKeys(executor, ms, parameter);
        }
    }

    private void processGeneratedKeys(Executor executor, MappedStatement ms, Object parameter) {
        try {
            if (parameter != null && keyStatement != null && keyStatement.getKeyProperties() != null) {
                //获取参数
                String[] keyProperties = keyStatement.getKeyProperties();
                //通过配置类获取执行和jdbc链接
                final Configuration configuration = ms.getConfiguration();
                final MetaObject metaParam = configuration.newMetaObject(parameter);
                if (keyProperties != null) {
                    Executor keyExecutor = configuration.newExecutor(executor.getTransaction());
                    //执行query
                    List<Object> values = keyExecutor.query(keyStatement, parameter, Executor.NO_RESULT_HANDLER, RowBounds.DEFAULT);
                    if (values.size() == 0) {
                        throw new RuntimeException("SelectKey returned no data.");
                    } else if (values.size() > 1) {
                        throw new RuntimeException("SelectKey returned more than one value.");
                    } else {

                        //通过创建反射工具类MetaObject，向对象的属性设置查询结果
                        MetaObject metaResult = configuration.newMetaObject(values.get(0));
                        if (keyProperties.length == 1) {
                            if (metaResult.hasGetter(keyProperties[0])) {
                                setValue(metaParam, keyProperties[0], metaResult.getValue(keyProperties[0]));
                            } else {
                                setValue(metaParam, keyProperties[0], values.get(0));
                            }
                        } else {
                            handleMultipleProperties(keyProperties, metaParam, metaResult);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error selecting key or setting result to parameter object. Cause: " + e);
        }
    }

    private void handleMultipleProperties(String[] keyProperties,
                                          MetaObject metaParam, MetaObject metaResult) {
        String[] keyColumns = keyStatement.getKeyColumns();

        if (keyColumns == null || keyColumns.length == 0) {
            for (String keyProperty : keyProperties) {
                setValue(metaParam, keyProperty, metaResult.getValue(keyProperty));
            }
        } else {
            if (keyColumns.length != keyProperties.length) {
                throw new RuntimeException("If SelectKey has key columns, the number must match the number of key properties.");
            }
            for (int i = 0; i < keyProperties.length; i++) {
                setValue(metaParam, keyProperties[i], metaResult.getValue(keyColumns[i]));
            }
        }
    }

    private void setValue(MetaObject metaParam, String property, Object value) {
        if (metaParam.hasSetter(property)) {
            metaParam.setValue(property, value);
        } else {
            throw new RuntimeException("No setter found for the keyProperty '" + property + "' in " + metaParam.getOriginalObject().getClass().getName() + ".");
        }
    }


}
