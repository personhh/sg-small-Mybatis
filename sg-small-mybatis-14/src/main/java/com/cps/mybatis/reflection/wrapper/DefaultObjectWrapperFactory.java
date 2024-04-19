package com.cps.mybatis.reflection.wrapper;

import com.cps.mybatis.reflection.MetaObject;

/**
 * @author cps
 * @description: TODO
 * @date 2024/1/20 20:00
 * @OtherDescription: Other things
 */
public class DefaultObjectWrapperFactory implements ObjectWrapperFactory{
    public DefaultObjectWrapperFactory() {
    }

    @Override
    public boolean hasWrapperFor(Object object) {
        return false;
    }

    @Override
    public ObjectWrapper getWrapperFor(MetaObject metaObject, Object object) {
        throw new RuntimeException("The DefaultObjectWrapperFactory should never be called to provide an ObjectWrapper.");
    }
}
