package pond.db;

import pond.common.S;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Date;

public class ResultSetGetters {

  public static Array getArray(ResultSet rs, String name) {
    return S._try_ret(() -> rs.getArray(name));
  }

  public static Array getArray(ResultSet rs, Integer index) {
    return S._try_ret(() -> rs.getArray(index));
  }

  public static String getString(ResultSet rs, String name) {
    return S._try_ret(() ->  rs.getObject(name) == null? null : rs.getString(name));
  }

  public static String getString(ResultSet rs, Integer name) {
    return S._try_ret(() ->  rs.getObject(name) == null? null : rs.getString(name));
  }

  public static InputStream getBinaryStream(ResultSet rs, String name) {
    return S._try_ret(() -> rs.getBinaryStream(name));
  }

  public static InputStream getAsciiStream(ResultSet rs, String name) {
    return S._try_ret(() -> rs.getAsciiStream(name));
  }

  public static Blob getBlob(ResultSet rs, String name) {
    return S._try_ret(() -> rs.getBlob(name));
  }

  public static Blob getBlob(ResultSet rs, Integer index) {
    return S._try_ret(() -> rs.getBlob(index));
  }

  public static BigDecimal getBigDecimal(ResultSet rs, String name) {
    return S._try_ret(() -> rs.getBigDecimal(name));
  }

  public static BigDecimal getBigDecimal(ResultSet rs, Integer index) {
    return S._try_ret(() -> rs.getBigDecimal(index));
  }

  public static Boolean getBoolean(ResultSet rs, String name) {
    return S._try_ret(() -> rs.getBoolean(name));
  }

  public static Boolean getBoolean(ResultSet rs, Integer index) {
    return S._try_ret(() -> rs.getBoolean(index));
  }

  public static Reader getCharacterStream(ResultSet rs, Integer index) {
    return S._try_ret(() -> rs.getCharacterStream(index));
  }

  public static Reader getCharacterStream(ResultSet rs, String index) {
    return S._try_ret(() -> rs.getCharacterStream(index));
  }

  public static Byte getByte(ResultSet rs, String name) {
    return S._try_ret(() -> rs.getObject(name) == null? null : rs.getByte(name));
  }

  public static Byte getByte(ResultSet rs, Integer name) {
    return S._try_ret(() -> rs.getObject(name) == null? null : rs.getByte(name));
  }

  public static byte[] getBytes(ResultSet rs, Integer name) {
    return S._try_ret(() -> rs.getBytes(name));
  }

  public static byte[] getBytes(ResultSet rs, String name) {
    return S._try_ret(() -> rs.getBytes(name));
  }

  public static java.sql.Date getSqlDate(ResultSet rs, String name) {
    return S._try_ret(() -> rs.getDate(name));
  }

  public static java.sql.Date getSqlDate(ResultSet rs, Integer name) {
    return S._try_ret(() -> rs.getDate(name));
  }

  public static Date getDate(ResultSet rs, Integer name) {
    return S._try_ret(() -> {
      java.sql.Date sql_date = rs.getDate(name);
      return new Date(sql_date.getTime());
    });
  }

  public static Date getDate(ResultSet rs, String name) {
    return S._try_ret(() -> {
      java.sql.Date sql_date = rs.getDate(name);
      return new Date(sql_date.getTime());
    });
  }

  public static Timestamp getTimestamp(ResultSet rs, String name) {
    return S._try_ret(() -> rs.getTimestamp(name));
  }

  public static Timestamp getTimestamp(ResultSet rs, Integer name) {
    return S._try_ret(() -> rs.getTimestamp(name));
  }

  public static Time getTime(ResultSet rs, Integer name) {
    return S._try_ret(() -> rs.getTime(name));
  }

  public static Time getTime(ResultSet rs, String name) {
    return S._try_ret(() -> rs.getTime(name));
  }

  public static Clob getClob(ResultSet rs, String name) {
    return S._try_ret(() -> rs.getClob(name));
  }

  public static Clob getClob(ResultSet rs, Integer name) {
    return S._try_ret(() -> rs.getClob(name));
  }

  public static Long getLong(ResultSet rs, Integer name) {
    return S._try_ret(() ->  rs.getObject(name) == null? null : rs.getLong(name));
  }

  public static Long getLong(ResultSet rs, String name) {
    return S._try_ret(() ->  rs.getObject(name) == null? null : rs.getLong(name));
  }

  public static Short getShort(ResultSet rs, String name) {
    return S._try_ret(() ->  rs.getObject(name) == null? null : rs.getShort(name));
  }

  public static Short getShort(ResultSet rs, Integer name) {
    return S._try_ret(() ->  rs.getObject(name) == null? null : rs.getShort(name));
  }

  public static Float getFloat(ResultSet rs, Integer name) {
    return S._try_ret(() ->  rs.getObject(name) == null? null : rs.getFloat(name));
  }

  public static Float getFloat(ResultSet rs, String name) {
    return S._try_ret(() ->  rs.getObject(name) == null? null : rs.getFloat(name));
  }

  public static Double getDouble(ResultSet rs, String name) {
    return S._try_ret(() ->  rs.getObject(name) == null? null : rs.getDouble(name));
  }

  public static Double getDouble(ResultSet rs, Integer name) {
    return S._try_ret(() ->  rs.getObject(name) == null? null : rs.getDouble(name));
  }

  public static Integer getInt(ResultSet rs, Integer name) {
    return S._try_ret(() ->  rs.getObject(name) == null? null : rs.getInt(name));
  }

  public static Integer getInt(ResultSet rs, String name) {
    return S._try_ret(() ->  rs.getObject(name) == null? null : rs.getInt(name));
  }
}
