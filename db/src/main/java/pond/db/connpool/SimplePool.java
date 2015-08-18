package pond.db.connpool;


import org.slf4j.Logger;
import pond.common.S;
import pond.db.DB;
import pond.db.RuntimeSQLException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

//TODO JNDI
public class SimplePool implements ConnectionPool {


  LinkedBlockingQueue<ConnectionWrapper> availableConnections;

//    ConcurrentLinkedQueue<Connection> workingConnections;
//    ConcurrentLinkedQueue<Connection> tempCreatedConnections;

  private int poolMaxSize = 10;

  //    private String driverClass;
  private String url;
  private String username;
  private String pass;

  public SimplePool() {
  }

  public void setLogger(Logger logger) {

  }


  public SimplePool config(String driver, String url, String username,
                           String password) {

    try {
      Class.forName(driver);
    } catch (ClassNotFoundException e) {
      DB.logger.error("ClassNotFound", e);
    }

    DB.logger.info("driver_class ->" + driver);

    this.username = username;

    this.pass = password;

    this.url = url;

    DB.logger.info("conn_url ->" + url);

    int size;
    int defaultInitSize = 5;
    if (poolMaxSize > defaultInitSize) {
      size = defaultInitSize;
    } else {
      size = poolMaxSize;
    }

    availableConnections = new LinkedBlockingQueue<>(size);

    try {
      for (int i = 0; i < size; i++) {
        availableConnections.put(S._try_ret(this::createWrapper));
      }
    } catch (InterruptedException e) {
      S._debug(DB.logger, logger -> {
        logger.debug("InterruptedException when initializing simplePool", e);
      });
    }

    return this;
  }

  public Connection physicalConnection() throws SQLException {
    return DriverManager.getConnection(url, username, pass);
  }

  private ConnectionWrapper createWrapper() throws SQLException {

    return new ConnectionWrapper(physicalConnection(), this);
  }

  public void releaseConnection(ConnectionWrapper connection) {

    try {
      availableConnections.put(connection);
    } catch (InterruptedException e) {
      S._debug(DB.logger, logger -> {
        logger.debug(e.toString(), e);
      });
    }
  }

  public Connection getConnection() throws SQLException {
    try {
      return availableConnections.take().refresh();
    } catch (InterruptedException e) {
      throw new RuntimeSQLException(e);
    }
  }


  @Override
  public void setMaxSize(Integer maxSize) {
    if (maxSize < 0) throw new RuntimeException("Invalid maxSize");
    DB.logger.info("maxSize -> " + maxSize);
    this.poolMaxSize = maxSize;
  }

  @Override
  public SimplePool loadConfig(Properties p) {
    String str_poolMaxSize = p.getProperty(ConnectionPool.MAXSIZE, "10");
    setMaxSize(S._try_ret(() -> Integer.parseInt(str_poolMaxSize)));
    String driverClass = p.getProperty(ConnectionPool.DRIVER);
    String url = p.getProperty(ConnectionPool.URL);
    String pass = p.getProperty(ConnectionPool.PASSWORD);
    String username = p.getProperty(ConnectionPool.USERNAME);
    config(driverClass, url, username, pass);
    return this;
  }


  public static MysqlSimplePoolBuilder mysql() {
    return new MysqlSimplePoolBuilder();
  }


}
