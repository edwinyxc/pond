package com.shuimin.pond.codec.db;

import com.shuimin.common.S;
import com.shuimin.common.util.logger.Logger;
import com.shuimin.pond.core.Server;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Date;

import static com.shuimin.common.S._throw;

/**
 * <pre>
 *     jdbc operator
 * </pre>
 */
public class JdbcOperator implements Closeable{

    public final Connection conn;
    private PreparedStatement pstmt = null;
    private ResultSet rs = null;
    Logger logger = Server.G.logger();

    final JdbcOperator outer = this;

//	//use under jdk -1.4
//	@SuppressWarnings("unused")
//	private Object guarder = new Object() {
//		public void finalize() throws Throwable
//		{
//			outer.disposeAll();
//		}
//	};

    public JdbcOperator(Connection conn) {
        this.conn = conn;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * release resource
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

    /**
     * @param sql
     * @param params 对象数组，用于按顺序放置到sql模板中,支持InputStream 和 基本数据类型
     *
     * @return
     */
    public int executeUpdate(String sql, Object[] params)
        throws SQLException {
        if (pstmt != null) {
            _closeStmt();
        }

        pstmt = conn.prepareStatement(sql);

        Object o;
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                o = params[i];
                if (o instanceof String) {
                    pstmt.setString(i + 1, (String) o);
                } else if (o instanceof InputStream) {
                    try {
                        pstmt.setBinaryStream(i + 1,
                            (InputStream) o, ((InputStream) o).available());
                    } catch (IOException ex) {
                        S._lazyThrow(ex);
                    }
                }
                else {
                    _debug("not supported type input " + o);
                   pstmt.setString(i + 1, o.toString());
                }

            }
        }
        _debug(pstmt);

        if (rs != null) {
            _closeRs();
        }
        return pstmt.executeUpdate();
    }

    public ResultSet executeQuery(String sql) {
        return executeQuery(sql, null);
    }

    /**
     * execute SQL statements to Query
     */
    public ResultSet executeQuery(String sql, String[] params) {
        try {
            if (pstmt != null) {
                _closeStmt();
            }
            pstmt = conn.prepareStatement(sql);

            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    pstmt.setString(i + 1, params[i]);
                }
            }

            _debug(pstmt);
            if (rs != null) {
                _closeRs();
            }
            rs = pstmt.executeQuery();
            return rs;
        } catch (SQLException s) {
            _throw(s);
        }
        return null;
    }

    public int executeUpdate(String sql) {
        try {
            return executeUpdate(sql, null);
        }catch (SQLException e) {
            _throw(e);
        }
        return -1;
    }

    //	/**
//	 * execute SQL statements to Update,Modify or Delete
//	 */
//	public int executeUpdate(String sql, String[] params) throws SQLException {
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
//		num = pstmt.executeUpdate();
//		return num;
//	}
    public void transactionStart() {
        try {
            synchronized (conn) {
                conn.setAutoCommit(false);
            }
        }catch (SQLException e) {
            _throw(e);
        }
    }

    public void transactionCommit() {
        try {
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
                    conn.close();
                }
            }
        }catch (SQLException e) {
            _throw(e);
        }
    }

    /**
     * <p>调用jdbc 的 RollBack
     * </p>
     */
    public void rollback() {
        try {
            conn.rollback();
        } catch (SQLException e) {
            _throw(e);
        }
    }

    private void _debug(Object o) {
        if (logger != null) {
            logger.debug(o);
        }
    }

    private void setParam(PreparedStatement pstmt, int idx, Object val)
        throws SQLException {
        if(setParam_try_primitive(pstmt,idx,val)) return;
    }

    private boolean setParam_try_common(PreparedStatement pstmt,
                                        int idx,
                                        Object o) throws SQLException {
        if( o instanceof String ) {
            pstmt.setString(idx, (String) o);
            return true;
        }
        //TODO: test this
        else if (o instanceof Date) {
            pstmt.setTimestamp(idx,new Timestamp(((Date) o).getTime()));
            return true;
        }
        //FIXME : unfinished
        return false;
    }

    private boolean setParam_try_primitive(PreparedStatement pstmt,
                                    int idx,
                                    Object o ) throws SQLException {
       if ( o instanceof Integer ) {
           pstmt.setInt(idx,(Integer)o);
           return true;
       }else if (o instanceof Long) {
           pstmt.setLong(idx, (Long)o);
           return true;
       }else if ( o instanceof Short){
           pstmt.setShort(idx, (Short)o);
           return true;
       }else if ( o instanceof Character) {
           pstmt.setString(idx,String.valueOf(o));
           return true;
       }else if ( o instanceof Float) {
           pstmt.setFloat(idx,(Float) o);
           return true;
       }else if ( o instanceof Double) {
           pstmt.setDouble(idx,(Double) o);
           return true;
       }else if (o instanceof Byte) {
           pstmt.setByte(idx,(Byte) o);
           return true;
       }else if (o instanceof Boolean) {
           pstmt.setBoolean(idx,(Boolean) o);
           return true;
       }
       return false;
    }


 }
