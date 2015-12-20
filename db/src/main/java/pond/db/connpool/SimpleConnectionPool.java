package pond.db.connpool;

import pond.common.S;
import pond.common.f.Function;
import pond.db.DB;
import pond.db.RuntimeSQLException;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

class SimpleConnectionPool extends AbstractConnectionPool{

  SimpleConnectionPool(){ }

  LinkedBlockingQueue<ConnectionWrapper> availableConnections;

  class ConnectionWrapper implements Connection {
    private Connection connection;

    ConnectionWrapper(Connection connection ) {
      this.connection = connection;
    }

    boolean isClosed = false;

    public boolean isPhysicallyClosed() throws SQLException {
      return connection.isClosed();
    }

    public void physicallyClose() throws SQLException {
      connection.close();
    }

    public ConnectionWrapper refresh() throws SQLException {
      if (connection == null || connection.isClosed()) {
        connection = SimpleConnectionPool.this.physicalConnection();
      }
      isClosed = false;
      return this;
    }

    @Override
    public void close() throws SQLException {
      isClosed = true;
      SimpleConnectionPool.this.releaseConnection(this);
    }

    @Override
    public boolean isClosed() throws SQLException {
      return isClosed;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> iface) throws SQLException {
      return (T) connection;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
      S._assert(iface);
      return iface.isAssignableFrom(connection.getClass());
    }

    @Override
    public Statement createStatement() throws SQLException {
      return connection.createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
      return connection.prepareStatement(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
      return connection.prepareCall(sql);
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
      return connection.nativeSQL(sql);
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
      connection.setAutoCommit(autoCommit);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
      return connection.getAutoCommit();
    }

    @Override
    public void commit() throws SQLException {
      connection.commit();
    }

    @Override
    public void rollback() throws SQLException {
      connection.rollback();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
      return connection.getMetaData();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
      connection.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() throws SQLException {
      return connection.isReadOnly();
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
      connection.setCatalog(catalog);
    }

    @Override
    public String getCatalog() throws SQLException {
      return connection.getCatalog();
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
      connection.setTransactionIsolation(level);
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
      return connection.getTransactionIsolation();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
      return connection.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
      connection.clearWarnings();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
      return connection.createStatement(resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
      return connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
      return connection.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
      return connection.getTypeMap();
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
      connection.setTypeMap(map);
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
      connection.setHoldability(holdability);
    }

    @Override
    public int getHoldability() throws SQLException {
      return connection.getHoldability();
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
      return connection.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
      return connection.setSavepoint(name);
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
      connection.rollback(savepoint);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
      connection.releaseSavepoint(savepoint);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
        throws SQLException {
      return connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType,
                                              int resultSetConcurrency, int resultSetHoldability) throws SQLException {
      return connection.prepareStatement(sql, resultSetType, resultSetHoldability);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType,
                                         int resultSetConcurrency, int resultSetHoldability) throws SQLException {
      return connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
      return connection.prepareStatement(sql, autoGeneratedKeys);

    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
      return connection.prepareStatement(sql, columnIndexes);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
      return connection.prepareStatement(sql, columnNames);
    }

    @Override
    public Clob createClob() throws SQLException {
      return connection.createClob();
    }

    @Override
    public Blob createBlob() throws SQLException {
      return connection.createBlob();
    }

    @Override
    public NClob createNClob() throws SQLException {
      return connection.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
      return connection.createSQLXML();
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
      return connection.isValid(timeout);
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
      connection.setClientInfo(name, value);
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
      connection.setClientInfo(properties);
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
      return connection.getClientInfo(name);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
      return connection.getClientInfo();
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
      return connection.createArrayOf(typeName, elements);
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
      return connection.createStruct(typeName, attributes);
    }

    @Override
    public void setSchema(String schema) throws SQLException {
      connection.setSchema(schema);
    }

    @Override
    public String getSchema() throws SQLException {
      return connection.getSchema();
    }

    @Override
    public void abort(Executor executor) throws SQLException {
      connection.abort(executor);
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
      connection.setNetworkTimeout(executor, milliseconds);
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
      return connection.getNetworkTimeout();
    }


  }

  public Connection physicalConnection() throws SQLException {
    return DriverManager.getConnection(this.conn_url, username, password);
  }

  private ConnectionWrapper createWrapper() throws SQLException {
    return new ConnectionWrapper(physicalConnection());
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

  public Connection _getConnection() throws SQLException {
    try {
      return availableConnections.take().refresh();
    } catch (InterruptedException e) {
      throw new RuntimeSQLException(e);
    }
  }

  @Override
  Function.F0ERR<Connection> build() {

    availableConnections = new LinkedBlockingQueue<>(max_size);

    try {
      for (int i = 0; i < max_size; i++) {
        availableConnections.put(S._try_ret(this::createWrapper));
      }
    } catch (InterruptedException e) {
      S._debug(DB.logger, logger -> {
        logger.debug("InterruptedException when initializing simplePool", e);
      });
    }

    return this::_getConnection;
  }

}
