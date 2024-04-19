package com.cps.mybatis.datasource.pooled;

import com.cps.mybatis.datasource.unpooled.UnpooledDataSourceFactory;

import javax.sql.DataSource;

//有池化数据源工厂
public class PooledDataSourceFactory extends UnpooledDataSourceFactory {

    public DataSource getDataSource(){
        PooledDataSource pooledDataSource = new PooledDataSource();
        pooledDataSource.setDriver(props.getProperty("driver"));
        pooledDataSource.setUrl(props.getProperty("url"));
        pooledDataSource.setUsername(props.getProperty("username"));
        pooledDataSource.setPassword(props.getProperty("password"));
        return pooledDataSource;
    }
}
