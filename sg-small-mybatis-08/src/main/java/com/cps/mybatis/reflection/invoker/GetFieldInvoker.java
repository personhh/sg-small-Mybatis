package com.cps.mybatis.reflection.invoker;

import java.lang.reflect.Field;

/**
 * @author cps
 * @description: 调用者（get方法）
 * @date 2024/1/19 21:03
 * @OtherDescription: getter 方法的调用者处理，因为get是有返回值的，所以直接对 Field 字段操作完后直接返回结果。
 */
public class GetFieldInvoker implements Invoker{

    private Field field;

    public GetFieldInvoker(Field field) {
        this.field = field;
    }

    @Override
    public Object invoke(Object target, Object[] args) throws Exception {
        return field.get(target);
    }

    @Override
    public Class<?> getType() {
        return field.getType();
    }
}
