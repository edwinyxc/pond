package pond.db.connpool;

import pond.db.sql.dialect.Dialect;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Created by ed on 6/6/14.
 * A SPI for DB module to get connection.
 * System do not need to know which or how the
 * connection established. Just get.
 */
public interface ConnectionPool extends DataSource {

  /**
   * in-properties label
   */

  String DRIVER = "ConnectionPool.driver";
  String URL = "ConnectionPool.url";
  String PASSWORD = "ConnectionPool.password";
  String USERNAME = "ConnectionPool.username";
  String MAXSIZE = "ConnectionPool.maxsize";

  void setMaxSize(Integer maxSize);

  Dialect dialect();
  ConnectionPool dialect(Dialect d);

  ConnectionPool loadConfig(Properties p);

  Connection getConnection() throws SQLException;


  @Override
  default Connection getConnection(String username, String password) throws SQLException {
    return getConnection();
  }

  @Override
  default PrintWriter getLogWriter() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  default void setLogWriter(PrintWriter out) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  default void setLoginTimeout(int seconds) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  default int getLoginTimeout() throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  default Logger getParentLogger() throws SQLFeatureNotSupportedException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  default <T> T unwrap(Class<T> iface) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  default boolean isWrapperFor(Class<?> iface) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  static Properties local(String tmp_db_name) {
    Properties ret = new Properties();

    ret.setProperty(DRIVER, "org.h2.Driver");
    ret.setProperty(URL, String.format("jdbc:h2:~/%s;MODE=MySQL;DATABASE_TO_UPPER=FALSE", tmp_db_name));
    ret.setProperty(USERNAME, "sa");
    ret.setProperty(PASSWORD, "sa");
    ret.setProperty(MAXSIZE, "300");

    return ret;
  }

//  static ConnectionPool mem(String tmp_db_name) {
//    return new SimpleConnectionPool().loadConfig(new Properties() {{
//      this.setProperty(DRIVER, "org.h2.Driver");
//      this.setProperty(URL, String.format("jdbc:h2:~/%s;MODE=MySQL;DATABASE_TO_UPPER=FALSE", tmp_db_name));
//      this.setProperty(USERNAME, "sa");
//      this.setProperty(PASSWORD, "sa");
//      this.setProperty(MAXSIZE, "10");
//    }});
//  }

  static Properties mysql() {
    Properties ret = new Properties();
    ret.setProperty(DRIVER, "com.mysql.jdbc.Driver");
    ret.setProperty(URL, "jdbc:mysql://localhost:3306");
    ret.setProperty(USERNAME, "root");
    ret.setProperty(PASSWORD, "root");
    ret.setProperty(MAXSIZE, "50");
    return ret;
  }

  static ConnectionPool simplePool(Properties p) {
    return new SimpleConnectionPool().loadConfig(p);
  }

  static ConnectionPool c3p0(Properties p) {
    return new C3p0ConnectionPool().loadConfig(p);
  }

}
