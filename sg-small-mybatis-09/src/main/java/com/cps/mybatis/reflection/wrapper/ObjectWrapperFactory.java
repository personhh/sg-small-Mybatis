package com.cps.mybatis.reflection.wrapper;

import com.cps.mybatis.reflection.MetaObject;

/**
 * @author cps
 * @description: 对象包装工厂
 * @date 2024/1/20 18:44
 * @OtherDescription: Other things
 */
public interface ObjectWrapperFactory {

    //判断有没有包装器
    boolean hasWrapperFor(Object object);

    //得到包装器
    ObjectWrapper getWrapperFor(MetaObject metaObject, Object object);
}
