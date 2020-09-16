package pond.db.connpool;

import pond.common.S;
import pond.common.f.Function;
import pond.db.DB;
import pond.db.sql.dialect.Dialect;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by ed on 12/18/15.
 */
@SuppressWarnings({"rawtypes"})
abstract class AbstractConnectionPool<T extends AbstractConnectionPool> implements ConnectionPool{

  String jdbc_driver;
  String conn_url;
  String username;
  String password;

  int max_size;

  boolean built;
  protected Properties properties;
  protected Function.F0ERR<Connection> connection_getter;

  public Dialect dialect;

  @SuppressWarnings("unchecked")
  public T driver(String jdbc_driver){
    this.jdbc_driver = jdbc_driver;
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T url(String url){
    this.conn_url= url;
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T username(String username) {
    this.username = username;
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T password(String password) {
    this.password = password;
    return (T) this;
  }

  @Override
  public void setMaxSize(Integer maxSize) {
    this.max_size = maxSize;
  }

  @Override
  public Dialect dialect() {
    return this.dialect;
  }

  @Override
  public ConnectionPool dialect(Dialect d) {
    this.dialect = d;
    return this;
  }
  @Override
  public ConnectionPool loadConfig(Properties p) {

    this.jdbc_driver = p.getProperty(DRIVER);
    try {
      Class.forName(jdbc_driver);
      //dialect
      switch (jdbc_driver) {
        case "com.mysql.jdbc.Driver": this.dialect = Dialect.mysql;break;
        case "org.h2.Driver": this.dialect = Dialect.h2;break;
      }
    } catch (ClassNotFoundException e) {
      DB.logger.error("ClassNotFound", e);
    }

    DB.logger.info("driver_class ->" + jdbc_driver);

    this.conn_url = p.getProperty(URL);
    DB.logger.info("conn_url ->" + conn_url);

    this.username = p.getProperty(USERNAME);

    this.password = p.getProperty(PASSWORD);

    this.max_size = Integer.parseInt(S.avoidNull(p.getProperty(MAXSIZE), "50"));

    return this;
  }

  protected boolean validateParams(){
    return jdbc_driver == null || conn_url == null || username == null || password == null;
  }

  abstract Function.F0ERR<Connection> build();

  @Override
  public Connection getConnection() throws SQLException {

    if(!built){
      connection_getter = build();
      built = true;
    }

    try {
      return connection_getter.apply();
    } catch (Exception e) {
      if(e instanceof SQLException){
        throw (SQLException) e;
      }
      throw new RuntimeException(e);
    }
  }

}
