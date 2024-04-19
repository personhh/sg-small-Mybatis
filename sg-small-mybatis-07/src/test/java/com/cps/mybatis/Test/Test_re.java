package com.cps.mybatis.Test;

import com.cps.mybatis.Test.Po.User;
import com.cps.mybatis.reflection.Reflector;
import com.cps.mybatis.reflection.invoker.MethodInvoker;
import org.junit.Test;

import java.lang.reflect.Field;

/**
 * @author cps
 * @description: TODO
 * @date 2024/2/27 19:13
 * @OtherDescription: Other things
 */
public class Test_re {

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        User user = new User();
        user.setUserName("ok");
        Field userName = user.getClass().getDeclaredField("userName");//获取user对象中属性的信息
        System.out.println(userName);
        userName.setAccessible(true);//正常通过反射获取私有变量会报错，因此加这一行来确保允许通过反射获取
        Object o = userName.get(user);
        System.out.println(o);
    }

    @Test
    public void test1() throws Exception{
        Field method = MethodInvoker.class.getDeclaredField("method");
        System.out.println("method : " + method);
        method.setAccessible(true);

    }
}
