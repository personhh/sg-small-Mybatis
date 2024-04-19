package com.cps.mybatis.reflection.invoker;

/**
 * @author cps
 * @description: 反射调用者接口
 * @date 2024/1/19 20:36
 * @OtherDescription: 无论任何类型的反射调用，都离不开对象和入参，只要我们把这两个字段和返回结果定义的通用，就可以包住不同策略的实现类了
 */
public interface Invoker {

    //反射方法入参执行
    Object invoke(Object target, Object[] args) throws Exception;

    //获取对象
    Class<?> getType();
}
