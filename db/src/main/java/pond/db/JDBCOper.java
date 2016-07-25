package pond.db;

import pond.common.S;
import pond.common.f.Callback;

import java.io.Closeable;
import java.io.InputStream;
import java.sql.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static pond.common.S.dump;

/**
 * <pre>
 *     jdbc operator
 * </pre>
 */
class JDBCOper implements Closeable {

  final private Connection conn;

  JDBCOper(Connection conn) {

    if (conn != null) {
      _closeConn();
    }
    this.conn = conn;
  }

//
//    /**
//     * <pre>
//     *     ResultSet#getObject 返回默认转换类型的值，通常是 java.sql.*类型
//     * 此方法转化成通用类型
//     * </pre>
//     *
//     * @param o in
//     * @return out
//     */
//    public static Object normalizeValue(Object o) {
//
//        if (o instanceof java.sql.Date) {
//            return new Date(((java.sql.Date) o).getTime());
//        }
//        if (o instanceof java.sql.Time) {
//            return new Date(((java.sql.Time) o).getTime());
//        }
//        if (o instanceof Timestamp) {
//            return new Date(((Timestamp) o).getTime());
//        }
//
//        return o;
//    }

  /**
   * release bo
   */
  @Override
  public void close() {
    _closeConn();
  }


  private void _closeConn() {
    try {
      if (conn != null && !conn.isClosed()) {
        conn.close();
      }
    } catch (SQLException e) {
      _debug("sql except when closing conn");
    }
  }

  /**
   * Get table names for the conn
   *
   * @throws SQLException
   */
  public Set<String> getTableNames() throws SQLException {
    Set<String> tables = new HashSet<>();
    DatabaseMetaData dbMeta = conn.getMetaData();
    ResultSet rs = dbMeta.getTables(
        null, null, "%", new String[]{"TABLE"}
    );
    while (rs.next()) {
      tables.add(rs.getString("table_name"));
    }
    return tables;
  }

  public Set<String> getTableAndViewNames() throws SQLException {
    Set<String> tables = new HashSet<>();
    DatabaseMetaData dbMeta = conn.getMetaData();
    ResultSet rs = dbMeta.getTables(
        null, null, "%", new String[]{"TABLE", "VIEW"}
    );
    while (rs.next()) {
      tables.add(rs.getString("table_name"));
    }
    return tables;
  }

  public void dropTable(String name) throws SQLException {
    execute(String.format("DROP TABLE IF EXISTS %s", name));
  }

  public void createIndex(String name, String table, String... columns)
      throws SQLException {
    execute(String.format("CREATE INDEX %s ON %s(%s)", name, table,
                          String.join(", ", Arrays.asList(columns))));
  }

  public void dropIndex(String name, String table) throws SQLException {
    execute(String.format("DROP INDEX %s", name));
  }

  /**
   * DANGER: SQL-INJECTION RISK
   */
  public int executeRawSQL(String sql) throws SQLException {
    boolean suc;
    int num;
    try (Statement st = conn.createStatement()) {
      suc = st.execute(sql);
      num = st.getUpdateCount();
      _debug("execute_raw " + (suc ? "success" : "failed") + " lines: " + num);
      return num;
    } catch (SQLException e) {
      throw e;
    }
  }

  /**
   * @param sql    sql
   * @param params array for parameters put into the sql, Only primitive types,
   *               String and InputStream are supported
   */
  public int execute(String sql, Object[] params, int[] types) throws SQLException {

    if (params == null) {
      throw new NullPointerException("params or types null");
    }

    if (params.length != types.length) {
      throw new IllegalArgumentException("Illegal arguments. Parameters and Types must have same length.");
    }

    if (conn == null || conn.isClosed()) {
      throw new SQLException("conn is already closed for this JDBCOper" + dump(this));
    }

    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      _debug(sql + "\n" + S.dump(params));

      for (int i = 0; i < params.length; i++) {
        if (params[i] == null)
          pstmt.setNull(i + 1, types[i]);
          //work around InputStream
        else if (params[i] instanceof InputStream)
          pstmt.setBinaryStream(i + 1, (InputStream) params[i]);
        else
        //TODO insertRecord configure
          pstmt.setObject(i + 1, params[i], types[i]);
        //这个函数有缺陷
//                setParam(pstmt, i + 1, params[i],tpyes[i]);
      }
      return S._tap(pstmt.executeUpdate(), affectedLineNum -> _debug("executed lines: [" + affectedLineNum + "]"));
    }


  }

//    public ResultSet query(String sql) throws SQLException {
//        return query(sql, null);
//    }

  /**
   * execute SQL statements to Query
   */
  public void query(String sql, Object[] params, Callback<ResultSet> cb) throws SQLException {

    _debug(sql + "\n" + S.dump(params));

    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      if (params != null) {
        for (int i = 0; i < params.length; i++) {
          //untested
          pstmt.setObject(i + 1, params[i]);
        }
      }
      try (ResultSet rs = pstmt.executeQuery()) {
        _debug(rs.toString());
        cb.apply(rs);
      }
    }
  }

  public int execute(String sql) throws SQLException {
    return execute(sql, new String[0], new int[0]);
  }

  public void transactionStart() throws SQLException {

    if (!conn.getAutoCommit()) {
      DB.logger.warn("autocommit has already been set to false.");
      S._debug(DB.logger, logger -> {
        logger.debug(dump(conn));
      });
    }

    conn.setAutoCommit(false);

  }

  public void transactionCommit() throws SQLException {
    conn.commit();
    //S.echo(conn.toString() + "closed :" + conn.isClosed());
    conn.setAutoCommit(true);
  }

  private void _debug(Object o) {
    S._debug(DB.logger, logger -> logger.debug(dump(o)));
  }


}
