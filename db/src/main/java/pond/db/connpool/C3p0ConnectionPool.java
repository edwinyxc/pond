package pond.db.connpool;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import pond.common.f.Function;

import java.beans.PropertyVetoException;
import java.sql.Connection;

/**
 * Created by ed on 12/18/15.
 */
class C3p0ConnectionPool extends AbstractConnectionPool {

  C3p0ConnectionPool() {}

  @Override
  Function.F0ERR<Connection> build() {

    ComboPooledDataSource cpds = new ComboPooledDataSource();

    try {
      cpds.setDriverClass(jdbc_driver); //loads the jdbc driver
    } catch (PropertyVetoException e) {
      throw new RuntimeException("jdbc init fail", e.getCause());
    }

    cpds.setJdbcUrl(conn_url);

    cpds.setUser(username);
    cpds.setPassword(password);

    cpds.setMaxPoolSize(max_size);

    //pstmt cache
    cpds.setMaxStatements(20 * max_size);

    return cpds::getConnection;
  }
}
