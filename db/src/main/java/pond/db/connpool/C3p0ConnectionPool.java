package pond.db.connpool;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import pond.common.f.Function;

import java.beans.PropertyVetoException;
import java.sql.Connection;

/**
 * Created by ed on 12/18/15.
 */
class C3p0ConnectionPool extends AbstractConnectionPool {

  final ComboPooledDataSource cpds = new ComboPooledDataSource();
  C3p0ConnectionPool() {}

  @Override
  Function.F0ERR<Connection> build() {


    try {
      cpds.setDriverClass(jdbc_driver); //loads the jdbc driver
    } catch (PropertyVetoException e) {
      throw new RuntimeException("jdbc init fail", e.getCause());
    }

    cpds.setJdbcUrl(conn_url);

    cpds.setUser(username);
    cpds.setPassword(password);

    cpds.setMaxPoolSize(max_size);

    //test every 60s
    cpds.setIdleConnectionTestPeriod(60);

    //wait 3000ms to get the connection or fail
    cpds.setCheckoutTimeout(3000);

//    cpds.setAcquireIncrement(10);

    cpds.setMaxIdleTime(300);

    //pstmt cache
    cpds.setMaxStatements(20 * max_size);

    return cpds::getConnection;
  }

  public ComboPooledDataSource rawC3p0(){
    return cpds;
  }
}
