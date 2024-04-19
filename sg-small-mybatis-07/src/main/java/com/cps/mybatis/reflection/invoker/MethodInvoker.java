package com.cps.mybatis.reflection.invoker;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

/**
 * @author cps
 * @description: 反射调用者实现类 （普通method）
 * @date 2024/1/19 20:47
 * @OtherDescription: Other things
 */
public class MethodInvoker implements Invoker{

    private Class<?> type;

    //反射方法类
    private Method method;

    public MethodInvoker(Method method){
        this.method = method;

        //如果只有一个参数，返回参数类型，否则返回方法返回值 类型
        if (method.getParameters().length == 1){
            //返回这个唯一参数的类型。
            type = method.getParameterTypes()[0];
        }else{
            //返回方法返回值类型
            type = method.getReturnType();
        }
    }

    //代理入参执行
    @Override
    public Object invoke(Object target, Object[] args) throws Exception {
        return method.invoke(target,args);
    }

    //返回入参类型或者返回值类型
    @Override
    public Class<?> getType() {
        return type;
    }
}
