package com.cps.mybatis.datasource.pooled;

import com.cps.mybatis.datasource.unpooled.UnpooledDataSourceFactory;

//有池化数据源工厂
public class PooledDataSourceFactory extends UnpooledDataSourceFactory {

    public PooledDataSourceFactory() {
        this.dataSource = new PooledDataSource();
    }
}
