package com.cps.mybatis.builder;

import com.cps.mybatis.session.Configuration;

public abstract class BaseBuilder {

    protected final Configuration configuration;

    public BaseBuilder(Configuration configuration) {
        this.configuration = configuration;
    }

    public Configuration getConfiguration(){
        return configuration;
    }
}
