package pond.db;

import pond.common.S;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class JDBCOper
        implements Closeable {

//    private static final Type[] SUPPORTED_TYPES = new Type[]{
//            Byte.TYPE, Character.TYPE, Short.TYPE, Integer.TYPE, Long.TYPE,
//            Float.TYPE, Double.TYPE, Boolean.TYPE, String.class, InputStream.class
//    };
    public final Connection conn;
    static Logger logger = LoggerFactory.getLogger(JDBCOper.class);
    private PreparedStatement pstmt = null;


    //	//use under jdk -1.4
//	@SuppressWarnings("unused")
//	private Object guarder = new Object() {
//		public void finalize() throws Throwable
//		{
//			outer.disposeAll();
//		}
//	};
    private ResultSet rs = null;

    public JDBCOper(Connection conn) {
        this.conn = conn;
    }

    /**
     * <pre>
     *     ResultSet#getObject 返回默认转换类型的值，通常是 java.sql.*类型
     * 此方法转化成通用类型
     * </pre>
     *
     * @param o in
     * @return out
     */
    public static Object normalizeValue(Object o) {

        if (o instanceof java.sql.Date) {
            return new Date(((java.sql.Date) o).getTime());
        }
        if (o instanceof java.sql.Time) {
            return new Date(((java.sql.Time) o).getTime());
        }
        if (o instanceof Timestamp) {
            return new Date(((Timestamp) o).getTime());
        }

        return o;
    }

    /**
     * release bo
     */
    @Override
    public void close() {
        _closeRs();
        _closeStmt();
        _closeConn();
    }

    private void _closeStmt() {
        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException e) {
                _debug("sql except when closing stmt");
            }
        }
    }

    private void _closeConn() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                _debug("sql except when closing conn");
            }
        }
    }

    private void _closeRs() {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                _debug("sql except when closing rs");
            }
        }
    }

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

//    @SuppressWarnings("unchecked")
//    public int execute(String sql, Callback.C3[] paramFuncs, Object[] params)
//            throws SQLException {
//        if (paramFuncs == null || params == null ||
//                paramFuncs.length != params.length) {
//            throw new IllegalArgumentException("functions,parameters must be non-none and with same length");
//        }
//        if (pstmt != null) {
//            _closeStmt();
//        }
//        pstmt = conn.prepareStatement(sql);
//        _debug(sql + "\n"
//                + S.dump(params));
//        for (int i = 0; i < params.length; i++) {
//            paramFuncs[i].apply(pstmt, i + 1, params[i]);
//        }
//
//        if (mapper != null) {
//            _closeRs();
//        }
//        return pstmt.executeUpdate();
//    }


//    public int execute(String sql,Object[] params) throws SQLException {
//        if (pstmt != null) {
//            _closeStmt();
//        }
//        pstmt = conn.prepareStatement(sql);
//
//        _debug(sql + "\n"
//                + S.dump(params));
//
//        if (params != null) {
//            for (int i = 0; i < params.length; i++) {
//                if (params[i] == null)
//                    pstmt.setNull(i + 1, types[i]);
//                else
//                    pstmt.setObject(i + 1, params[i], types[i]);
//                //这个函数有缺陷
////                setParam(pstmt, i + 1, params[i],tpyes[i]);
//            }
//        }
//
//        if (rs != null) {
//            _closeRs();
//        }
//        return pstmt.executeUpdate();
//    }

    /**
     * @param sql    sql
     * @param params 对象数组，用于按顺序放置到sql模板中,支持InputStream 和 基本数据类型
     */
//     * @deprecated Deprecated because we cannot distinguish 'null' value for 'set it null' or for 'leave it for now'.
//     * Use execute(String sql, Functions, params) instead
//    @Deprecated
    public int execute(String sql, Object[] params, int[] types) throws SQLException {

        if (params == null){
            throw new NullPointerException("params or types null");
        }

        if (params.length != types.length) {
            throw new IllegalArgumentException("Illegal arguments. Parameters and Types must have same length.");
        }
        if (pstmt != null) {
            _closeStmt();
        }
        pstmt = conn.prepareStatement(sql);

        _debug(sql + "\n"
                + S.dump(params));

        for (int i = 0; i < params.length; i++) {
            if (params[i] == null)
                pstmt.setNull(i + 1, types[i]);
                //work around InputStream
            else if (params[i] instanceof InputStream)
                pstmt.setBinaryStream(i + 1, (InputStream) params[i]);
            else
                pstmt.setObject(i + 1, params[i], types[i]);
            //这个函数有缺陷
//                setParam(pstmt, i + 1, params[i],tpyes[i]);
        }

        if (rs != null) {
            _closeRs();
        }
        return pstmt.executeUpdate();
    }

    public ResultSet query(String sql) throws SQLException {
        return query(sql, null);
    }

    /**
     * execute SQL statements to Query
     */
    public ResultSet query(String sql, Object[] params) throws SQLException {
        if (pstmt != null) {
            _closeStmt();
        }
        pstmt = conn.prepareStatement(sql);

        _debug(sql + "\n"
                + S.dump(params));

        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                //untested
                pstmt.setObject(i + 1, params[i]);
            }
        }


        if (rs != null) {
            _closeRs();
        }
        rs = pstmt.executeQuery();
        return rs;
    }

    public int execute(String sql) throws SQLException {
        return execute(sql, new String[0], new int[0] );
    }

    //	/**
//	 * execute SQL statements to Update,Modify or Delete
//	 */
//	public int execute(String sql, String[] params) throws SQLException {
//		int num = 0;
//		if (pstmt != null) {
//			_closeStmt();
//		}
//		pstmt = conn.prepareStatement(sql);
//
//		if (params != null) {
//			for (int i = 0; i < params.length; i++) {
//				pstmt.setString(i + 1, params[i]);
//			}
//		}
//
//		_debug(pstmt);
//		num = pstmt.execute();
//		return num;
//	}
    public void transactionStart() throws SQLException {
        synchronized (conn) {
            conn.setAutoCommit(false);
        }
    }

    public void transactionCommit() throws SQLException {
        try {
            synchronized (conn) {
                conn.commit();
            }
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            synchronized (conn) {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * <p>调用jdbc 的 RollBack
     * </p>
     */
    public void rollback() throws SQLException {
        conn.rollback();
    }

    private void _debug(Object o) {
        if (logger != null) {
            logger.info(dump(o));
        }
    }

    void setParam(PreparedStatement pstmt, int idx, Object val, int type)
            throws SQLException {
//        if (val == null)
//            //这里这样处理是因为实在区分不出默认情况下，null值是用于“清除值”还是“未改变”
//            //所以最简单的办法就是留给用户处理
//            throw new NullPointerException("Please use default value instead of null");
        /**
         * 2014-8-20更新：
         * 出现null值就意味着需要置空。
         * 表示不改变不是jdbcOper的责任，应该由SQL层负责
         */
        if (val == null) {
            pstmt.setNull(idx, type);
        }else {
            pstmt.setObject(idx, val, type);
        }

    }


    /*
     This`ve been commented because the dependency of UploadFile have been
     moved.
     */
//    private boolean setParam_try_UploadFile(PreparedStatement pstmt,
//                                            int idx,
//                                            Object o) throws SQLException {
//        if (o instanceof UploadFile) {
//            pstmt.setBinaryStream(idx, ((UploadFile) o).inputStream());
//            return true;
//        }
//        return false;
//    }

//    private static boolean setParam_try_common(PreparedStatement pstmt,
//                                               int idx,
//                                               Object o) throws SQLException {
//        if (o instanceof String) {
//            pstmt.setString(idx, (String) o);
//            return true;
//        }
////        else if (o instanceof Date) {
////            pstmt.setTimestamp(idx,new Timestamp(((Date) o).getTime()));
////            return true;
////        }
//
//        if (o instanceof InputStream) {
//            pstmt.setBinaryStream(idx, (InputStream) o);
//            return true;
//        }
//
//        return false;
//    }
//
//    private static boolean setParam_try_primitive(PreparedStatement pstmt,
//                                                  int idx,
//                                                  Object o) throws SQLException {
//        if (o instanceof Integer) {
//            pstmt.setInt(idx, (Integer) o);
//            return true;
//        } else if (o instanceof Long) {
//            pstmt.setLong(idx, (Long) o);
//            return true;
//        } else if (o instanceof Short) {
//            pstmt.setShort(idx, (Short) o);
//            return true;
//        } else if (o instanceof Character) {
//            pstmt.setString(idx, String.valueOf(o));
//            return true;
//        } else if (o instanceof Float) {
//            pstmt.setFloat(idx, (Float) o);
//            return true;
//        } else if (o instanceof Double) {
//            pstmt.setDouble(idx, (Double) o);
//            return true;
//        } else if (o instanceof Byte) {
//            pstmt.setByte(idx, (Byte) o);
//            return true;
//        } else if (o instanceof Boolean) {
//            pstmt.setBoolean(idx, (Boolean) o);
//            return true;
//        } else if (o instanceof BigDecimal) {
//            pstmt.setBigDecimal(idx, (BigDecimal) o);
//        }
//        return false;
//    }


}
