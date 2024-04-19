package com.cps.mybatis.reflection.invoker;

import java.lang.reflect.Method;

/**
 * @author cps
 * @description: 反射调用者实现类 （普通method）
 * @date 2024/1/19 20:47
 * @OtherDescription: Other things
 */
public class MethodInvoker implements Invoker{

    private Class<?> type;
    private Method method;

    public MethodInvoker(Method method){
        this.method = method;

        //如果只有一个参数，返回参数类型，否则返回return 类型
        if (method.getParameters().length == 1){
            type = method.getParameterTypes()[0];
        }else{
            type = method.getReturnType();
        }
    }
    @Override
    public Object invoke(Object target, Object[] args) throws Exception {
        return method.invoke(target,args);
    }

    @Override
    public Class<?> getType() {
        return type;
    }
}
