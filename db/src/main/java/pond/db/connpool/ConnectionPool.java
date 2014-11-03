package pond.db.connpool;

import pond.common.f.Function;

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
    public static String DRIVER = "ConnectionPool.driver";
    public static String URL= "ConnectionPool.url";
    public static String PASSWORD = "ConnectionPool.password";
    public static String USERNAME = "ConnectionPool.username";
    public static String MAXSIZE = "ConnectionPool.maxsize";

    public void setMaxSize(Integer maxSize);

//    public void setCache(Function<Connection,String>, );

    public ConnectionPool loadConfig(Properties p);

    Connection getConnection() throws SQLException;

    @Override default
    Connection getConnection(String username, String password) throws SQLException {
        return  getConnection();
    }

    @Override default
    PrintWriter getLogWriter() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override default
    void setLogWriter(PrintWriter out) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override default
    void setLoginTimeout(int seconds) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override default
    int getLoginTimeout() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override default
    Logger getParentLogger() throws SQLFeatureNotSupportedException{
        throw new SQLFeatureNotSupportedException();
    }

    @Override default
    <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override default
    boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
}
