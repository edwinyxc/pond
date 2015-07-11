package pond.db.connpool;

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
}
