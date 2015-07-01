package pond.db.connpool;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.S;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static pond.common.S._dump;

public class SimplePool implements ConnectionPool {

    static private Logger logger = LoggerFactory.getLogger(SimplePool.class);

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


    public SimplePool config ( String driver, String url, String username,
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

        connPool = new ArrayList<>();
        int size;
        int defaultInitSize = 5;
        if (poolMaxSize > defaultInitSize) {
            size = defaultInitSize;
        } else {
            size = poolMaxSize;
        }
        for (int i = 0; i < size; i++) {
            connPool.add(S._try_ret(() -> createConnection()));
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
                    logger.debug(_dump(connection));
                }
                // do nothing
            }
            return;
        }
        connPool.add(connection);
    }

    public synchronized Connection getConnection() {
        if( connPool == null
            || driverClass == null
            || url == null
            || username == null
            || pass == null
        ) throw new RuntimeException("Please config first");
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
            throw new RuntimeException(e);
        }
    }


    @Override
    public void setMaxSize(Integer maxSize) {
        if( maxSize < 0 || maxSize > 200) throw new RuntimeException("Invalid maxSize");
        logger.info("maxSize -> "+ maxSize);
        this.poolMaxSize = maxSize;
    }

    @Override
    public SimplePool loadConfig(Properties p) {
        String str_poolMaxSize = p.getProperty(ConnectionPool.MAXSIZE, "10");
        setMaxSize(S._try_ret( () -> Integer.parseInt(str_poolMaxSize) ));
        String driverClass = p.getProperty(ConnectionPool.DRIVER);
        String url = p.getProperty(ConnectionPool.URL);
        String pass = p.getProperty(ConnectionPool.PASSWORD);
        String username = p.getProperty(ConnectionPool.USERNAME);
        config(driverClass,url,username,pass);
        return this;
    }


    public static MysqlSimplePoolBuilder Mysql(){
        return new MysqlSimplePoolBuilder();
    }


}
