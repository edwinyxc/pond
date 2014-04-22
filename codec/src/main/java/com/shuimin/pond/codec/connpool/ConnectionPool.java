package com.shuimin.pond.codec.connpool;

import com.shuimin.common.util.logger.Logger;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.shuimin.common.S._throw;

public class ConnectionPool {

	private Logger logger = Logger.get();
	private List<Connection> connPool;
	private int poolMaxSize = 10;

    private String driverClass;
	private String url;
	private String username;
	private String pass;

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public ConnectionPool() {

	}

	public ConnectionPool init(ConnectionConfig config)
			throws SQLException {

		String maxSize = config.maxPoolSize;
		if (maxSize != null) {
			poolMaxSize = Integer.parseInt(maxSize);
		}
		logger.debug("max_pool_size ->" + poolMaxSize);

		driverClass = config.driverClass;

		logger.debug("driver_class ->" + driverClass);

		username = config.username;

		logger.debug("username ->" + username);

		pass = config.password;

		logger.debug("password ->" + pass);

		url = config.connectionUrl;

		logger.debug("conn_url ->" + url);
		// poolMaxSize = pool_max_size;
		// userName = user_name;
		// this.password = password;
		// this.driverClass = driver_class;
		// this.url = conn_url;
		//

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
		try {
			Class.forName(driverClass);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
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
        }catch (SQLException e) {
            _throw (e);
        }
        return null;
	}
}
