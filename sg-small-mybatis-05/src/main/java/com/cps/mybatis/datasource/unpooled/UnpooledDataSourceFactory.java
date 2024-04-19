package com.cps.mybatis.datasource.unpooled;

import com.cps.mybatis.datasource.DataSourceFactory;

import javax.sql.DataSource;
import java.util.Properties;

//无池话数据源工厂
public class UnpooledDataSourceFactory implements DataSourceFactory {

    protected Properties props;
    @Override
    public void setProperties(Properties props) {
        this.props = props;
    }

    @Override
    public DataSource getDataSource() {
        UnpoopledDataSource unpoopledDataSource = new UnpoopledDataSource();
        unpoopledDataSource.setDriver(props.getProperty("driver"));
        unpoopledDataSource.setUrl(props.getProperty("url"));
        unpoopledDataSource.setUsername(props.getProperty("username"));
        unpoopledDataSource.setPassword(props.getProperty("password"));
        return unpoopledDataSource;
    }
}
