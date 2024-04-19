package com.cps.mybatis.datasource.pooled;


import com.cps.mybatis.datasource.unpooled.UnpoopledDataSource;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.logging.Logger;


//有池化数据源实现
public class PooledDataSource implements DataSource {

    private org.slf4j.Logger logger = LoggerFactory.getLogger(PooledDataSource.class);

    //池状态
    private final PoolState state = new PoolState(this);

    private final UnpoopledDataSource dataSource;

    // 活跃连接数
    protected int poolMaximumActiveConnections = 10;
    // 空闲连接数
    protected int poolMaximumIdleConnections = 5;
    // 在被强制返回之前,池中连接被检查的时间
    protected int poolMaximumCheckoutTime = 20000;
    // 这是给连接池一个打印日志状态机会的低层次设置,还有重新尝试获得连接, 这些情况下往往需要很长时间 为了避免连接池没有配置时静默失败)。
    protected int poolTimeToWait = 20000;
    // 发送到数据的侦测查询,用来验证连接是否正常工作,并且准备 接受请求。默认是“NO PING QUERY SET” ,这会引起许多数据库驱动连接由一 个错误信息而导致失败
    protected String poolPingQuery = "NO PING QUERY SET";
    // 开启或禁用侦测查询
    protected boolean poolPingEnabled = false;
    // 用来配置 poolPingQuery 多次时间被用一次
    protected int poolPingConnectionsNotUsedFor = 0;

    private int expectedConnectionTypeCode;

    public PooledDataSource() {
        this.dataSource = new UnpoopledDataSource();
    }

    /*回收链接*/
    protected void pushConnection(PooledConnection connection) throws SQLException{
        synchronized (state) {
            state.activeConnections.remove(connection);
            //判断链接是否有效
                if (connection.isValid()) {
                    //如果空闲链接小于设定数量，也就是太少时
                    if (state.idleConnections.size() < poolMaximumIdleConnections && connection.getConnectionTypeCode() == expectedConnectionTypeCode) {
                        state.accumulatedCheckoutTime += connection.getCheckoutTime();
                        //它首先检查数据库连接是否处于自动提交模式，如果不是，则调用rollback()方法执行回滚操作。
                        // 在MyBatis中，如果没有开启自动提交模式，则需要手动提交或回滚事务。因此，这段代码可能是在确保操作完成后，如果没有开启自动提交模式，则执行回滚操作。
                        // 总的来说，这段代码用于保证数据库的一致性，确保操作完成后，如果未开启自动提交模式，则执行回滚操作。
                        if (!connection.getRealconnection().getAutoCommit()) {
                            connection.getRealconnection().rollback();
                        }

                        //实例化一个新的DB连接，加入到空闲的队列中
                        PooledConnection newConnection = new PooledConnection(connection.getRealconnection(), this);
                        state.idleConnections.add(newConnection);
                        newConnection.setCreateTimestamp(connection.getCreateTimestamp());
                        newConnection.setLastUsedTimestamp(connection.getLastUsedTimestamp());
                        connection.invalidate();
                        logger.info("Returned connection " + newConnection.getRealHashCode() + " to pool");

                        //通知别的线程可以来墙DB连接了
                        state.notify();
                    }
                    //否则，空闲链接还比较充足
                    else {
                        state.accumulatedCheckoutTime += connection.getCheckoutTime();
                        if (!connection.getRealconnection().getAutoCommit()) {
                            connection.getRealconnection().rollback();
                        }
                        //将connection关闭
                        connection.getRealconnection().close();
                        logger.info("Closed connection " + connection.getRealHashCode() + ".");
                        connection.invalidate();
                    }
                } else {
                    logger.info("A bad connection (" + connection.getRealHashCode() + ") attempted to return to the pool, discarding connection");
                    state.badConnectionCount++;
                }
            }
        }

    //获取链接

    private PooledConnection popConnection(String username, String password) throws SQLException {
        PooledConnection conn = null;
        boolean countedWait = false;
        long t = System.currentTimeMillis();
        int localBadConnectionCount = 0;
        while (conn == null) {
            synchronized (state) {

                //如果有空闲链接，返回第一个
                if (!state.idleConnections.isEmpty()) {
                    conn = state.idleConnections.remove(0);
                    logger.info("Checked out connection" + conn.getRealHashCode() + " from pool.");
                }

                //如果无空闲链接，创建新的链接
                else {
                    //活跃链接数不足
                    if (state.activeConnections.size() < poolMaximumActiveConnections) {
                        conn = new PooledConnection(dataSource.getConnection(), this);
                        logger.info("Checked out connection " + conn.getRealHashCode() + ".");
                    }
                    //活跃连接数已满
                    else {
                        //取得活跃连连接列表的第一个，也就是最老的一个连接
                        PooledConnection oldestActiveConnection = state.activeConnections.get(0);
                        long longestCheckoutTime = oldestActiveConnection.getCheckoutTime();
                        //如果checkout时间过长，则这个连接标记为过长，标记为过期
                        if (longestCheckoutTime > poolMaximumCheckoutTime) {
                            state.claimedOverdueConnectionCount++;
                            state.accumulatedCheckoutTimeOfOverdueConnections += longestCheckoutTime;
                            state.accumulatedCheckoutTime += longestCheckoutTime;
                            state.activeConnections.remove(oldestActiveConnection);
                            if (!oldestActiveConnection.getRealconnection().getAutoCommit()) {
                                oldestActiveConnection.getRealconnection().rollback();
                            }
                            //删除最老的连接，然后重新实例化一个新的连接
                            conn = new PooledConnection(oldestActiveConnection.getRealconnection(), this);
                            oldestActiveConnection.invalidate();
                            logger.info("Claimed overdue connection " + conn.getRealHashCode() + ".");
                        }

                        //如果checkout超时时间不够长，则等待
                        else {
                            try {
                                if (!countedWait) {
                                    state.hadToWaitCount++;
                                    countedWait = true;
                                }
                                logger.info("Waiting as long as" + poolTimeToWait + " milliseconds for connection.");
                                long wt = System.currentTimeMillis();
                                state.wait(poolTimeToWait);
                                state.accumulatedWaitTime += System.currentTimeMillis() - wt;
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                    }
                }
                //获得到链接
                if (conn != null) {
                    //链接不为空
                    if (conn.isValid()) {
                        //如果链接合法
                        if (!conn.getRealconnection().getAutoCommit()) {
                            //判断链接是否自动提交，如果不是就要触发回滚
                            conn.getRealconnection().rollback();
                        }
                        //设置链接的标识码
                        conn.setConnectionTypeCode(assembleConnectionTypeCode(dataSource.getUrl(), username, password));
                        // 记录checkout时间
                        conn.setCheckoutTimestamp(System.currentTimeMillis());
                        //记录链接最近的时间
                        conn.setLastUsedTimestamp(System.currentTimeMillis());
                        //添加到活跃链接队列中
                        state.activeConnections.add(conn);
                        //池内请求链接加1
                        state.requestCount++;
                        //池内累计请求链接时间
                        state.accumulatedRequestTime += System.currentTimeMillis() - t;
                    } else {
                        logger.info("A bad connection (" + conn.getRealHashCode() + ") was returned from the pool, getting another connection.");
                        //如果没拿到，统计信息，失败链接 +1
                        state.badConnectionCount++;
                        localBadConnectionCount++;
                        conn = null;
                        //失败次数较多，抛异常
                        if (localBadConnectionCount > (poolMaximumIdleConnections + 3)) {
                            logger.debug("PooledDataSource: Could not get a good connection to the database.");
                            throw new SQLException("PooledDataSource: Could not get a good connection to the database.");
                        }
                    }
                }
            }
        }

        if (conn == null) {
            logger.debug("PooledDataSource: Unknown severe error condition. The connection pool return a null connection");
            throw new SQLException("PooledDataSource: Unknown severe error condition. The connection pool returned a null connection");
        }
        return conn;
    }

    //强制关闭所有链接
    public void forceCloseAll() {
        synchronized (state){
           expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
           //关闭活跃链接
            for(int i = state.activeConnections.size(); i > 0; i--){
                try{
                    PooledConnection conn = state.activeConnections.remove(i - 1);
                    conn.invalidate();

                    Connection realConn = conn.getRealconnection();
                    if(!realConn.getAutoCommit()){
                        realConn.rollback();
                    }
                }catch (Exception ignore){

                }
            }

            //关闭空闲链接
            for(int i = state.idleConnections.size(); i > 0; i--){
                try{
                    PooledConnection conn = state.idleConnections.remove(i - 1);
                    conn.invalidate();

                    Connection realConn = conn.getRealconnection();
                    if(!realConn.getAutoCommit()){
                        realConn.rollback();
                    }
                }catch (Exception ignore){

                }
            }


            logger.info("PooledDataSource forcefully closed/removed all connections");
        }
    }

    //ping链接
    protected boolean pingConnection(PooledConnection conn){
        boolean result = true;
        try{
            result = !conn.getRealconnection().isClosed();
        }catch (SQLException e){
            logger.info("Connection " + conn.getRealHashCode() + "is BAD: "  + e.getMessage());
            result = false;
        }

        if(result){
            if(poolPingEnabled) {
                if(poolPingConnectionsNotUsedFor >= 0 && conn.getTimeElapsedSinceLastUse() > poolPingConnectionsNotUsedFor){
                    try{
                        logger.info("Testing connection " + conn.getRealHashCode() + " ...");
                        Connection realConn = conn.getRealconnection();
                        Statement statement = realConn.createStatement();
                        ResultSet resultSet = statement.executeQuery(poolPingQuery);
                        resultSet.close();
                        if(!realConn.getAutoCommit()){
                            realConn.rollback();
                        }
                        result = true;
                        logger.info("Connection "  + conn.getRealHashCode() + " is GOOD!");
                    }catch (Exception e){
                        logger.info("Execution of ping query '" + poolPingQuery + "' failed: " + e.getMessage());
                        try{
                            conn.getRealconnection().close();
                        }catch(SQLException ignore){

                        }
                        result = false;
                        logger.info("Connection " + conn.getRealHashCode() + "is BAD: " + e.getMessage());
                    }
                }
            }
        }
        return result;
    }

    public static Connection unwrapConnection(Connection conn) {
        if (Proxy.isProxyClass(conn.getClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(conn);
            if (handler instanceof PooledConnection) {
                return ((PooledConnection) handler).getRealconnection();
            }
        }
        return conn;
    }

    private int assembleConnectionTypeCode(String url, String username, String password) {
        return ("" + url + username + password).hashCode();
    }

    @Override
    protected void finalize() throws Throwable{
        forceCloseAll();
        super.finalize();
    }

    public String getDriver() {
        return dataSource.getDriver();
    }

    public String getUrl() {
        return dataSource.getUrl();
    }

    public String getUsername() {
        return dataSource.getUsername();
    }

    public String getPassword() {
        return dataSource.getPassword();
    }


    public void setDriver(String driver){
        dataSource.setDriver(driver);
    }

    public void setUrl(String url){
        dataSource.setUrl(url);
        forceCloseAll();
    }

    public void setUsername(String username){
        dataSource.setUsername(username);
        forceCloseAll();
    }

    public void setPassword(String password){
        dataSource.setPassword(password);
        forceCloseAll();
    }

    public void setDefaultAutoCommit(boolean defaultAutoCommit){
        dataSource.setAutoCommit(defaultAutoCommit);
        forceCloseAll();
    }
    @Override
    public Connection getConnection() throws SQLException {
        return popConnection(dataSource.getUsername(),dataSource.getPassword()).getProxyConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return popConnection(dataSource.getUsername(),dataSource.getPassword()).getProxyConnection();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException(getClass().getName() + " is not a wrapper.");
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
    public void setLogWriter(PrintWriter out) throws SQLException {
        DriverManager.setLogWriter(out);
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

    public PoolState getState() {
        return state;
    }

    public UnpoopledDataSource getDataSource() {
        return dataSource;
    }

    public int getPoolMaximumActiveConnections() {
        return poolMaximumActiveConnections;
    }

    public void setPoolMaximumActiveConnections(int poolMaximumActiveConnections) {
        this.poolMaximumActiveConnections = poolMaximumActiveConnections;
    }

    public int getPoolMaximumIdleConnections() {
        return poolMaximumIdleConnections;
    }

    public void setPoolMaximumIdleConnections(int poolMaximumIdleConnections) {
        this.poolMaximumIdleConnections = poolMaximumIdleConnections;
    }

    public int getPoolMaximumCheckoutTime() {
        return poolMaximumCheckoutTime;
    }

    public void setPoolMaximumCheckoutTime(int poolMaximumCheckoutTime) {
        this.poolMaximumCheckoutTime = poolMaximumCheckoutTime;
    }

    public int getPoolTimeToWait() {
        return poolTimeToWait;
    }

    public void setPoolTimeToWait(int poolTimeToWait) {
        this.poolTimeToWait = poolTimeToWait;
    }

    public String getPoolPingQuery() {
        return poolPingQuery;
    }

    public void setPoolPingQuery(String poolPingQuery) {
        this.poolPingQuery = poolPingQuery;
    }

    public boolean isPoolPingEnabled() {
        return poolPingEnabled;
    }

    public void setPoolPingEnabled(boolean poolPingEnabled) {
        this.poolPingEnabled = poolPingEnabled;
    }

    public int getPoolPingConnectionsNotUsedFor() {
        return poolPingConnectionsNotUsedFor;
    }

    public void setPoolPingConnectionsNotUsedFor(int poolPingConnectionsNotUsedFor) {
        this.poolPingConnectionsNotUsedFor = poolPingConnectionsNotUsedFor;
    }

    public int getExpectedConnectionTypeCode() {
        return expectedConnectionTypeCode;
    }

    public void setExpectedConnectionTypeCode(int expectedConnectionTypeCode) {
        this.expectedConnectionTypeCode = expectedConnectionTypeCode;
    }
}
