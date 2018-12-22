package pond.db;


import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Types;

import static pond.common.f.Function.F2;

public class MappingRule {

  /*BASIC JDBC types*/
  final public Object getNilOfType(int sqlType) {
    switch (sqlType) {
      case Types.CHAR:
        return '\0';
      case Types.VARCHAR:
      case Types.LONGVARCHAR:
        return "";
      case Types.BIT:
        return Boolean.FALSE;
      case Types.TINYINT:
      case Types.SMALLINT:
      case Types.INTEGER:
        return 0;
      case Types.BIGINT:
        return 0L;
      case Types.REAL:
        return 0f;
      case Types.DOUBLE:
      case Types.FLOAT:
        return 0d;
      case Types.DECIMAL:
      case Types.NUMERIC:
        return new BigDecimal(0);
      case Types.DATE:
      case Types.TIME:
      case Types.TIMESTAMP:
        return new java.sql.Date(0L);

      //TODO advanced types

      /*
       * [ADVANCED] -- not supported
       *
       * BLOB
       * CLOB
       * ARRAY
       * REF
      case Types.BLOB:
        return BLOB();
      case Types.CLOB:
        return CLOB();
      case Types.ARRAY:
        return ARRAY();
        */
    }
    throw new IllegalArgumentException("Unknown type: " + sqlType);
  }

  final public F2<?, ResultSet, String> getMethod(int sqlType) {
    switch (sqlType) {
      case Types.CHAR:
        return CHAR();
      case Types.VARCHAR:
        return VARCHAR();
      case Types.LONGVARCHAR:
        return LONGVARCHAR();
      case Types.BIT:
        return BIT();
      case Types.TINYINT:
        return TINYINT();
      case Types.SMALLINT:
        return SMALLINT();
      case Types.INTEGER:
        return INTEGER();
      case Types.BIGINT:
        return BIGINT();
      case Types.REAL:
        return REAL();
      case Types.DOUBLE:
        return DOUBLE();
      case Types.FLOAT:
        return FLOAT();
      case Types.DECIMAL:
        return DECIMAL_NUMERIC();
      case Types.NUMERIC:
        return DECIMAL_NUMERIC();
      case Types.DATE:
        return DATE();
      case Types.TIME:
        return TIME();
      case Types.TIMESTAMP:
        return TIMESTAMP();

      //TODO advanced types

            /*
            * [ADVANCED]
            *
            * BLOB
            * CLOB
            * ARRAY
            * REF
            */
      case Types.BLOB:
        return BLOB();
      case Types.CLOB:
        return CLOB();
      case Types.ARRAY:
        return ARRAY();
      case Types.NVARCHAR:
        return VARCHAR();
    }
    throw new IllegalArgumentException("Unknown type: " + sqlType);
  }

  final public F2<?, ResultSet, Integer> getMethod_index(int sqlType) {
    switch (sqlType) {
      case Types.CHAR:
        return CHAR_index();
      case Types.VARCHAR:
        return VARCHAR_index();
      case Types.LONGVARCHAR:
        return LONGVARCHAR_index();
      case Types.BIT:
        return BIT_index();
      case Types.TINYINT:
        return TINYINT_index();
      case Types.SMALLINT:
        return SMALLINT_index();
      case Types.INTEGER:
        return INTEGER_index();
      case Types.BIGINT:
        return BIGINT_index();
      case Types.REAL:
        return REAL_index();
      case Types.DOUBLE:
        return DOUBLE_index();
      case Types.FLOAT:
        return FLOAT_index();
      case Types.DECIMAL:
        return DECIMAL_NUMERIC_index();
      case Types.NUMERIC:
        return DECIMAL_NUMERIC_index();
      case Types.DATE:
        return DATE_index();
      case Types.TIME:
        return TIME_index();
      case Types.TIMESTAMP:
        return TIMESTAMP_index();

      //TODO advanced types

            /*
            * [ADVANCED]
            *
            * BLOB
            * CLOB
            * ARRAY
            * REF
            */
      case Types.BLOB:
        return BLOB_index();
      case Types.CLOB:
        return CLOB_index();
      case Types.ARRAY:
        return ARRAY_index();
    }
    throw new IllegalArgumentException("Unknown type: " + sqlType);
  }

  protected F2<?, ResultSet, String> ARRAY() {
    return ResultSetGetters::getArray;
  }

  protected F2<?, ResultSet, Integer> ARRAY_index() {
    return ResultSetGetters::getArray;
  }

  protected F2<?, ResultSet, String> CLOB() {
    return ResultSetGetters::getClob;
  }

  protected F2<?, ResultSet, Integer> CLOB_index() {
    return ResultSetGetters::getClob;
  }

  protected F2<?, ResultSet, String> BLOB() {
    return ResultSetGetters::getBlob;
  }

  protected F2<?, ResultSet, Integer> BLOB_index() {
    return ResultSetGetters::getBlob;
  }

  protected F2<?, ResultSet, String> TIMESTAMP() {
    return ResultSetGetters::getTimestamp;
  }

  protected F2<?, ResultSet, Integer> TIMESTAMP_index() {
    return ResultSetGetters::getTimestamp;
  }

  protected F2<?, ResultSet, String> TIME() {
    return ResultSetGetters::getTime;
  }

  protected F2<?, ResultSet, Integer> TIME_index() {
    return ResultSetGetters::getTime;
  }

  protected F2<?, ResultSet, String> DATE() {
    return ResultSetGetters::getSqlDate;
  }

  protected F2<?, ResultSet, Integer> DATE_index() {
    return ResultSetGetters::getSqlDate;
  }

  protected F2<?, ResultSet, String> DECIMAL_NUMERIC() {
    return ResultSetGetters::getBigDecimal;
  }

  protected F2<?, ResultSet, Integer> DECIMAL_NUMERIC_index() {
    return ResultSetGetters::getBigDecimal;
  }

  protected F2<?, ResultSet, String> FLOAT() {
    return ResultSetGetters::getDouble;
  }

  protected F2<?, ResultSet, Integer> FLOAT_index() {
    return ResultSetGetters::getDouble;
  }

  protected F2<?, ResultSet, String> DOUBLE() {
    return ResultSetGetters::getDouble;
  }

  protected F2<?, ResultSet, Integer> DOUBLE_index() {
    return ResultSetGetters::getDouble;
  }

  protected F2<?, ResultSet, String> REAL() {
    return ResultSetGetters::getFloat;
  }

  protected F2<?, ResultSet, Integer> REAL_index() {
    return ResultSetGetters::getFloat;
  }

  protected F2<?, ResultSet, String> BIGINT() {
    return ResultSetGetters::getLong;
  }

  protected F2<?, ResultSet, Integer> BIGINT_index() {
    return ResultSetGetters::getLong;
  }

  protected F2<?, ResultSet, String> INTEGER() {
    return ResultSetGetters::getInt;
  }

  protected F2<?, ResultSet, Integer> INTEGER_index() {
    return ResultSetGetters::getInt;
  }

  protected F2<?, ResultSet, String> SMALLINT() {
    return ResultSetGetters::getShort;
  }

  protected F2<?, ResultSet, Integer> SMALLINT_index() {
    return ResultSetGetters::getShort;
  }

  protected F2<?, ResultSet, String> TINYINT() {
    return ResultSetGetters::getShort;
  }

  protected F2<?, ResultSet, Integer> TINYINT_index() {
    return ResultSetGetters::getShort;
  }

  protected F2<?, ResultSet, String> BIT() {
    return ResultSetGetters::getBoolean;
  }

  protected F2<?, ResultSet, Integer> BIT_index() {
    return ResultSetGetters::getBoolean;
  }

  protected F2<?, ResultSet, String> CHAR() {
    return ResultSetGetters::getString;
  }

  protected F2<?, ResultSet, Integer> CHAR_index() {
    return ResultSetGetters::getString;
  }

  protected F2<?, ResultSet, String> VARCHAR() {
    return ResultSetGetters::getString;
  }

  protected F2<?, ResultSet, Integer> VARCHAR_index() {
    return ResultSetGetters::getString;
  }

  protected F2<?, ResultSet, String> LONGVARCHAR() {
    return ResultSetGetters::getString;
  }

  protected F2<?, ResultSet, Integer> LONGVARCHAR_index() {
    return ResultSetGetters::getString;
  }
}
