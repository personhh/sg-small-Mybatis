package com.cps.mybatis.builder;

import com.cps.mybatis.session.Configuration;
import com.cps.mybatis.type.TypeAliasRegistry;

public abstract class BaseBuilder {

    protected final Configuration configuration;
    protected final TypeAliasRegistry typeAliasRegistry;
    public BaseBuilder(Configuration configuration) {
        this.configuration = configuration;
        this.typeAliasRegistry = this.configuration.getTypeAliasRegistry();
    }

    public Configuration getConfiguration(){
        return configuration;
    }
}
