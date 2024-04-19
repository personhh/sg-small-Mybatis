package com.cps.mybatis.reflection.invoker;

import java.lang.reflect.Field;

/**
 * @author cps
 * @description: 调用者（set方法）
 * @date 2024/1/19 21:11
 * @OtherDescription: setter 方法的调用者处理，因为set只是设置值，所以这里就只返回一个 null 就可以了。
 */
public class SetFieldInvoker implements Invoker{

    private Field field;

    public SetFieldInvoker(Field field) {
        this.field = field;
    }

    @Override
    public Object invoke(Object target, Object[] args) throws Exception {
     field.set(target, args[0]);
     return null;
    }

    @Override
    public Class<?> getType() {
        return field.getType();
    }
}
