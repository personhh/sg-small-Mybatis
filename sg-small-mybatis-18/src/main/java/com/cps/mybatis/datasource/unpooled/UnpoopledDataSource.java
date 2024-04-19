package com.cps.mybatis.datasource.unpooled;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

//无池化数据源实现

public class UnpoopledDataSource implements DataSource {

    private ClassLoader driverClassLoader;
    private String driver;
    private String url;
    private String username;
    private String password;
    private Boolean autoCommit;
    private Integer defaultTransactionIsolationLevel;
    private static Map<String, Driver> registeredDrivers = new ConcurrentHashMap<>();
    /**
     * 驱动的配置属性，也可以扩展属性信息 driver.encoding=UTF-8
     */
    private Properties driverProperties;

    //通过静态块来注册所有的驱动到驱动注册器中
    static {
        /**Enumeration 接口中定义了一些方法，通过这些方法可以遍历集合中的元素。*/
        Enumeration<Driver> drivers = DriverManager.getDrivers();//通过驱动管理器获得驱动
        while (drivers.hasMoreElements()) {
            //如果drivers中有驱动，那么一个个注册到驱动注册器中registeredDrivers
            Driver driver = drivers.nextElement();
            registeredDrivers.put(driver.getClass().getName(), driver);
        }
    }

    public UnpoopledDataSource() {
    }


    public UnpoopledDataSource(String driver, String url, String username, String password) {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
    }


    //驱动代理类
    private static class DriverProxy implements Driver {
        /**
         * 每个驱动程序都应提供一个实现Driver接口的类
         */

        private Driver driver;

        DriverProxy(Driver driver) {
            this.driver = driver;
        }

        /**
         * 尝试与给定的URL建立数据库连接。
         */
        @Override
        public Connection connect(String u, Properties p) throws SQLException {
            return this.driver.connect(u, p);
        }

        /**
         * 检索驱动程序是否认为它可以打开与给定URL的连接。
         */
        @Override
        public boolean acceptsURL(String u) throws SQLException {
            return this.driver.acceptsURL(u);
        }

        /**
         * 获取有关此驱动程序的可能属性的信息。
         */
        @Override
        public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) throws SQLException {
            return this.driver.getPropertyInfo(u, p);
        }

        /**
         * 检索驱动程序的主要版本号。
         */
        @Override
        public int getMajorVersion() {
            return this.driver.getMajorVersion();
        }

        /**
         * 获取驱动程序的次要版本号。
         */
        @Override
        public int getMinorVersion() {
            return this.driver.getMinorVersion();
        }

        /**
         * 报告此驱动程序是否是真正的JDBC Compliant™驱动程序。
         */
        @Override
        public boolean jdbcCompliant() {
            return this.driver.jdbcCompliant();
        }

        /**
         * 返回此驱动程序使用的所有记录器的父记录器。
         */
        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        }
    }

    //初始化驱动
    private synchronized void initializerDriver() throws SQLException {
        if (!registeredDrivers.containsKey(driver)) {
            try {
                Class<?> driverType = Class.forName(driver, true, driverClassLoader);
                Driver driverInstance = (Driver) driverType.newInstance();
                registeredDrivers.put(driver, driverInstance);
                DriverManager.registerDriver(new DriverProxy(driverInstance));
            } catch (Exception e) {
                throw new SQLException("Error setting driver on UnpooledDataSource. Cause: " + e);
            }
        }
    }

    private Connection doGetConnection(String username, String password) throws SQLException {
        Properties properties = new Properties();
        if (driverProperties != null) {
            properties.putAll(driverProperties);
        }
        if (username != null) {
            properties.setProperty("user", username);
        }
        if (password != null) {
            properties.setProperty("password", password);
        }
        return doGetConnection(properties);
    }

    private Connection doGetConnection(Properties properties) throws SQLException {
        initializerDriver();
        Connection connection = DriverManager.getConnection(url, properties);
        if (autoCommit != null && autoCommit != connection.getAutoCommit()) {
            connection.setAutoCommit(autoCommit);
        }
        if (defaultTransactionIsolationLevel != null) {
            connection.setTransactionIsolation(defaultTransactionIsolationLevel);
        }
        return connection;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return doGetConnection(username, password);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return doGetConnection(username, password);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException(getClass().getName() + "is not a wrapper.");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return DriverManager.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter logWriter) throws SQLException {
        DriverManager.setLogWriter(logWriter);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        DriverManager.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return DriverManager.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public Boolean getAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(Boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public ClassLoader getDriverClassLoader() {
        return driverClassLoader;
    }

    public void setDriverClassLoader(ClassLoader driverClassLoader) {
        this.driverClassLoader = driverClassLoader;
    }

    public Integer getDefaultTransactionIsolationLevel() {
        return defaultTransactionIsolationLevel;
    }

    public void setDefaultTransactionIsolationLevel(Integer defaultTransactionIsolationLevel) {
        this.defaultTransactionIsolationLevel = defaultTransactionIsolationLevel;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static Map<String, Driver> getRegisteredDrivers() {
        return registeredDrivers;
    }

    public static void setRegisteredDrivers(Map<String, Driver> registeredDrivers) {
        UnpoopledDataSource.registeredDrivers = registeredDrivers;
    }

    public Properties getDriverProperties() {
        return driverProperties;
    }

    public void setDriverProperties(Properties driverProperties) {
        this.driverProperties = driverProperties;
    }
}
