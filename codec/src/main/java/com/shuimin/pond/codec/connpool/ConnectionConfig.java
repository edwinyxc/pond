package com.shuimin.pond.codec.connpool;

/**
 * Created by ed on 2014/4/11.
 */
public class ConnectionConfig {
    public final String maxPoolSize;
    public final String driverClass;
    public final String username;
    public final String password;
    public final String connectionUrl;

    public ConnectionConfig(String maxPoolSize, String driverClass, String username, String password, String connectionUrl) {
        this.maxPoolSize = maxPoolSize;
        this.driverClass = driverClass;
        this.username = username;
        this.password = password;
        this.connectionUrl = connectionUrl;
    }


    @Override
    public String toString() {
        return "ConnectionConfig{" +
            "maxPoolSize='" + maxPoolSize + '\'' +
            ", driverClass='" + driverClass + '\'' +
            ", username='" + username + '\'' +
            ", password='" + password + '\'' +
            ", connectionUrl='" + connectionUrl + '\'' +
            '}';
    }
}
