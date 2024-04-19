package com.cps.mybatis.reflection;

import com.cps.mybatis.reflection.factory.DefaultObjectFactory;
import com.cps.mybatis.reflection.factory.ObjectFactory;
import com.cps.mybatis.reflection.wrapper.DefaultObjectWrapperFactory;
import com.cps.mybatis.reflection.wrapper.ObjectWrapperFactory;

/**
 * @author cps
 * @description: 一些系统级别的元对象
 * @date 2024/1/20 19:58
 * @OtherDescription:
 */
public class SystemMetaObject {

    //定义默认的对象工厂对象
    public static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();

    //定义默认的对象包装工厂对象
    public static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();

    //定义空的元对象
    public static final MetaObject NULL_META_OBJECT = MetaObject.forObject(NullObject.class, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY);

    private SystemMetaObject() {
        // Prevent Instantiation of Static Class
    }

    /**
     * 空对象
     */
    private static class NullObject {
    }

    //转化成元对象
    public static MetaObject forObject(Object object) {
        return MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY);
    }


}
