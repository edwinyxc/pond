package pond.db.spi.connpool;

import pond.common.S;
import pond.common.util.logger.Logger;
import pond.db.spi.ConnectionPool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static pond.common.S._throw;

public class SimplePool implements ConnectionPool {

    static private Logger logger = Logger.create(SimplePool.class);
    private List<Connection> connPool;
    private int poolMaxSize = 10;

    private String driverClass;
    private String url;
    private String username;
    private String pass;

    public SimplePool() {
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    @Override 
    public void init( Properties p ) {
    }

    @Override
    public void init( String driver, String url, String username,
            String password ) {

        this.driverClass = driver;

        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


        logger.info("driver_class ->" + driverClass);

        this.username = username;

        this.pass = password;

        this.url = url;

        logger.info("conn_url ->" + url);
    }

    public SimplePool init(ConnectionConfig config)
            throws SQLException {

        poolMaxSize = config.maxPoolSize <= 0 ? 5 : config.maxPoolSize;

        logger.info("max_pool_size ->" + poolMaxSize);

        init( config.driverClass,
              config.connectionUrl,
              config.username,
              config.password );


        connPool = new ArrayList<>();
        int size;
        int defaultInitSize = 5;
        if (poolMaxSize > defaultInitSize) {
            size = defaultInitSize;
        } else {
            size = poolMaxSize;
        }
        for (int i = 0; i < size; i++) {
            connPool.add(createConnection());
        }

        return this;
    }

    private Connection createConnection() throws SQLException {
        Connection connection;
        connection = DriverManager.getConnection(url, username, pass);
        return connection;
    }

    public synchronized void releaseConnection(Connection connection) {
        int size = connPool.size();
        if (size > poolMaxSize) {
            try {
                ConnectionProxy handler = (ConnectionProxy) Proxy
                        .getInvocationHandler(connection);
                handler.getConnection().close();// THE REAL
                // CLOSE
            } catch (SQLException e) {
                if (logger != null) {
                    logger.debug("connection can not close :");
                    logger.debug(connection);
                }
                // do nothing
            }
            return;
        }
        connPool.add(connection);
    }

    public synchronized Connection getConnection() {
        try {
            ConnectionProxy connectionProxy = new ConnectionProxy(this);
            int size = connPool.size();
            if (connPool.size() == 0 || size > poolMaxSize) {
                connectionProxy.setConnection(createConnection());
                return connectionProxy.proxyBind();
            }
            Connection connection;
            int i = 1;
            for (connection = connPool.get(size - i); i <= size; i++) {
                if (connection.isClosed()) {
                    connPool.remove(size - i);
                } else {
                    break;
                }
            }
            if (!connection.isClosed()) {
                connectionProxy.setConnection(connection);
            } else {
                connectionProxy.setConnection(createConnection());
            }
            return connectionProxy.proxyBind();
        } catch (SQLException e) {
            _throw(e);
        }
        return null;
    }
}
