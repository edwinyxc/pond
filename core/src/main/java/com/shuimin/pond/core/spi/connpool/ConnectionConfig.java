package com.shuimin.pond.core.spi.connpool;

import java.util.Properties;

/**
 * Created by ed on 2014/4/11.
 */
public class ConnectionConfig extends Properties{

    public final static String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
    public final static String MAX_SIZE = "connpool.max_size";
    public final static String DRIVER_CLASS = "connpool.driver_class";
    public final static String USERNAME ="connpool.username";
    public final static String PASSWORD = "connpool.password";
    public final static String CONNECTION_URL = "connpool.conn";

    public int maxPoolSize = 5;

    public String driverClass;

    public String username;

    public String connectionUrl;

    public String password;

    public ConnectionConfig(Properties prop) {
        this.maxPoolSize = Integer.parseInt(prop.getProperty(MAX_SIZE));
        this.driverClass = prop.getProperty(DRIVER_CLASS);
        this.username = prop.getProperty(USERNAME);
        this.password = prop.getProperty(PASSWORD);
        this.connectionUrl = prop.getProperty(CONNECTION_URL);
    }

    public ConnectionConfig(String maxPoolSize, String driverClass, String username, String password, String connectionUrl) {
        this.maxPoolSize = Integer.parseInt(maxPoolSize);
        this.driverClass = driverClass;
        this.username = username;
        this.password = password;
        this.connectionUrl = connectionUrl;
    }

    public ConnectionConfig(Integer maxPoolSize, String driverClass, String username, String password, String connectionUrl) {
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
