package com.cps.mybatis.datasource.pooled;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

//池化代理的链接
public class PooledConnection implements InvocationHandler {

    private static final String CLOSE = "close";
    private static final Class<?>[] IFACES = new Class<?>[]{Connection.class};

    private int hashCode = 0;
    private PooledDataSource dataSource;
    //真实的连接
    private Connection realconnection;
    //代理的连接
    private Connection proxyConnection;
    //连接取出时间戳
    private long checkoutTimestamp;
    //创建时间
    private long createTimestamp;
    //最近使用时间
    private long lastUsedTimestamp;
    //连接的标识码
    private int connectionTypeCode;
    //连接是否合法
    private boolean valid;


    public PooledConnection(Connection connection,PooledDataSource dataSource) {
        this.dataSource = dataSource;
        this.hashCode = connection.hashCode();
        this.realconnection  = connection;
        this.valid = true;
        this.createTimestamp = System.currentTimeMillis();
        this.lastUsedTimestamp = System.currentTimeMillis();
        this.proxyConnection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),IFACES,this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        //如果时调用CLOSE 关闭链接方法，则将链接加入连接池中，并返回null
        if(CLOSE.hashCode() == methodName.hashCode() && CLOSE.equals(methodName)){
            dataSource.pushConnection(this);
            return null;
        }else{
            if(!Object.class.equals(method.getDeclaringClass())){
                //除了toString方法，其他方法调用之前要检查Connection是否还是合法的，不合法就抛出SQLException
                checkConnection();
            }
        }
        //其他方法交给connection去调用
        return method.invoke(realconnection,args);
    }

    private  void checkConnection() throws SQLException{
        if(!valid){
            throw new SQLException("Error accessing PooledConnection. Connection is invalid");
        }
    }

    public void invalidate(){
        valid = false;
    }

    public boolean isValid(){
        return valid && realconnection != null && dataSource.pingConnection(this);
    }

    public Connection  getRealconnection(){
        return realconnection;
    }

    public Connection getProxyConnection(){
        return proxyConnection;
    }

    public int getRealHashCode(){
        return realconnection == null ? 0 : realconnection.hashCode();
    }

    public int getConnectionTypeCode(){
        return connectionTypeCode;
    }

    public void setConnectionTypeCode(int connectionTypeCode){
        this.connectionTypeCode = connectionTypeCode;
    }

    public long getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(long createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public long getLastUsedTimestamp() {
        return lastUsedTimestamp;
    }

    public void setLastUsedTimestamp(long lastUsedTimestamp) {
        this.lastUsedTimestamp = lastUsedTimestamp;
    }

    public long getTimeElapsedSinceLastUse(){
        return System.currentTimeMillis() - lastUsedTimestamp;
    }
    public long getAge(){
        return System.currentTimeMillis() - createTimestamp;
    }

    public long getCheckoutTimestamp(){
        return checkoutTimestamp;
    }

    public void setCheckoutTimestamp(long timestamp){
        this.checkoutTimestamp = timestamp;
    }

    public long getCheckoutTime(){
        return System.currentTimeMillis() - checkoutTimestamp;
    }

    @Override
    public int hashCode(){
        return hashCode;
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof PooledConnection){
            return realconnection.hashCode() == ((PooledConnection) obj).realconnection.hashCode();
        }
        else if (obj instanceof Connection){
            return hashCode == obj.hashCode();
        }else {
            return false;
        }
    }

}
