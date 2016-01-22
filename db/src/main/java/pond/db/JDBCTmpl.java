package pond.db;

import pond.common.ARRAY;
import pond.common.S;
import pond.common.f.Callback;
import pond.common.f.Function;
import pond.common.f.Tuple;
import pond.db.sql.*;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

import static pond.common.S._for;

/**
 * Created by ed on 2014/4/18.
 * <pre>
 *      JDBCTmpl
 *      jdbc- template
 *      A Common template of data access with database
 *      using JDBC.
 *  </pre>
 */
public class JDBCTmpl implements Closeable {

  //database structure
  //table_name -> Map<field_name, field_type(java.sql.Type)>
  final Map<String, Map<String, Integer>> dbStructure;

  private static final
  Function<Integer, ResultSet> counter = rs -> {
    try {
      return (Integer) rs.getInt(1);
    } catch (SQLException e) {
      throw new RuntimeSQLException(e);
    }
  };

  private final JDBCOper oper;
  private final DB db;

  JDBCTmpl(DB db, Connection connection) {
    this.db = db;
    this.oper = new JDBCOper(connection);
    this.dbStructure = db.dbStructures;
  }

  //protected tx control functions

  void txStart() throws SQLException {
    oper.transactionStart();
  }

  void txCommit() throws SQLException {
    oper.transactionCommit();
  }


  public int[] getTypes(String table, String[] keys) {
    S._assert(table);
    S._assert(keys);
    int[] types = new int[keys.length];
    for (int i = 0; i < types.length; i++) {
      types[i] = getType(table, keys[i]);
    }
    return types;
  }

  public int getType(String table, String field) {
    if (dbStructure == null)
      throw new RuntimeException(" dbStructure must not null");
    Integer ret = dbStructure.getOrDefault(table, Collections.emptyMap()).get(field);
    if (ret == null)
      throw new IllegalArgumentException("Cannot find field in dbStructure: " + field);
    return ret;
  }


  public List<Record> query(String sql, Object... x) {
    return this.query(db.default_row_mapper, sql, x);
  }


  public List<Record> query(String sql) {
    return this.query(db.default_row_mapper, sql);
  }

  @SuppressWarnings("unchecked")
  public <R extends Record> List<R> query(
      R proto,
      String sql, Object... x
  ) {
    return this.query(proto.mapper(), sql, x);
  }

  @SuppressWarnings("unchecked")
  public <R extends Record> List<R> query(
      Class<R> clazz,
      String sql, Object... x
  ) {
    R proto = Prototype.proto(clazz);
    return query(proto, sql, x);
  }

  public int count(String sql, Object... params) {
    return S.avoidNull(S.<Integer>_for(query(counter, sql,
                                             params)).first(), 0);
  }

  public int count(Tuple<String, Object[]> mix) {
    return count(mix._a, mix._b);
  }

// removed page function
//    public <R> Page<R> page(Function<?, ResultSet> mapper,
//                            Page<R> page ) {
//        List<R> r = query(rs_mapper, page.querySql());
//        int count = (Integer) _for(query(counter,page.querySql())).limit();
//        return page.fulfill(r, count);
//    }

  public <R extends Record> List<R> query(Class<R> clazz,
                                          Tuple<String, Object[]> mix) {
    return query(Prototype.proto(clazz), mix._a, mix._b);
  }

  public <R extends Record> List<R> query(Class<R> clazz, SqlSelect t) {
    return query(clazz, t.tuple());
  }

  public List<Record> query(SqlSelect t) {
    return query(t.tuple());
  }

  public List<Record> query(Tuple<String, Object[]> mix) {
    return query(db.default_row_mapper, mix);
  }

  public <R> List<R> query(Function<?, ResultSet> mapper,
                           Tuple<String, Object[]> mix) {
    return query(mapper, mix._a, mix._b);
  }

  @SuppressWarnings("unchecked")
  public <R> List<R> query(Function<?, ResultSet> mapper,
                           String sql, Object... args) {


    List<R> list = new ArrayList<>();
    long start = S.now();
    S._try(() -> oper.query(sql, args, rs -> {
             S._debug(DB.logger, log -> log.debug("time cost for creating resultSet: " + (S.now() - start)));
             S._try(() -> {
               while (rs.next()) {
                 //check
                 list.add((R) mapper.apply(rs));
               }
             });
           })
    );
    return list;
  }


  /**
   * execute sql without prepared-statement
   */
  public int exec(String sql) {
    try {
      return oper.execute(sql);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public int exec(Tuple<String, Object[]> t) {
    return exec(t._a, t._b);
  }

  public int exec(SqlInsert t) {
    return exec(t.tuple());
  }

  public int exec(SqlUpdate t) {
    return exec(t.tuple());
  }

  public int exec(SqlDelete t) {
    return exec(t.tuple());
  }

  public int exec(String sql, Object[] params, int[] types) {
    try {
      return oper.execute(sql, params, types);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }


  public int execRaw(String sql) {
    try {
      return oper.executeRawSQL(sql);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * execute sql & params
   *
   * @param sql    sql
   * @param params params
   * @return affected row number
   */
  public int exec(String sql, Object... params) {
    try {
      int[] types = new int[params.length];
      for (int i = 0; i < params.length; i++) {
        Object o = params[i];
        if (o == null)
          throw new IllegalArgumentException("Do not use null values without specify its types.");
        Class<?> clazz = params[i].getClass();
        types[i] = default_sql_type(clazz);
      }
      return oper.execute(sql, params, types);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  static int default_sql_type(Class<?> cls) {
    if (cls.equals(Integer.class) || cls.equals(int.class)) {
      return Types.INTEGER;
    } else if (cls.equals(Long.class) || cls.equals(long.class)) {
      //long -> big int works in mysql
      return Types.BIGINT;
    } else if (cls.equals(Double.class) || cls.equals(double.class)) {
      return Types.DOUBLE;
    } else if (cls.equals(Float.class) || cls.equals(float.class)) {
      return Types.FLOAT;
    } else if (cls.equals(Short.class) || cls.equals(short.class)) {
      return Types.SMALLINT;
    } else if (cls.equals(Boolean.class) || cls.equals(boolean.class)) {
      return Types.BOOLEAN;
    } else if (cls.equals(String.class)) {
      return Types.VARCHAR;
    } else if (cls.equals(Character.class) || cls.equals(char.class)) {
      return Types.VARCHAR;
    } else if (cls.equals(InputStream.class)) {
      return Types.BLOB;
    } else if (cls.equals(BigDecimal.class)) {
      return Types.DECIMAL;
    }
    throw new IllegalArgumentException("Class " + cls.toString() + " not supported,please specify input types.");
  }

  public void queryRaw(String sql, Callback<ResultSet> doWithResultSet) {
    queryRaw(sql, new String[0], doWithResultSet);
  }

  public void queryRaw(String sql, Object[] args, Callback<ResultSet> doWithResultSet) {
    try {
      oper.query(sql, args, doWithResultSet);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public <E extends Record> E recordById(Class<E> clazz, String id) {
    Record r = Prototype.proto(clazz);
    String tableName = r.table();
    String pkLbl = r.idName();
    SqlSelect select =
        Sql.select().from(tableName).where(pkLbl, Criterion.EQ, id);
    return S._for(this.query(clazz, select.tuple())).first();

  }


    /*----CURD
    Record#db
    -------*/

  /**
   * <pre>
   * Query record(s) on specified arguments
   * if there is no arguments, return Select all;
   *
   * if only one argument, the argument will treated as raw sql
   *  i.e. queryRS("id = '123' AND something LIKE '%else%' ")
   *      => Select ... from ... where id = '123' AND something LIKE '%else%';
   *
   * if arguments has a 3-multiple length and each 3 has a form of
   * (String,Criterion,String)  or (String,Criterion,String[])
   *  i.e.
   *  recordsQuery("id", Criterion.EQ, "13")
   *      => Select ... from  ... where id = '13'
   *  recordsQuer("id",Criterion.IN, {"1","2","3","4"})
   *      => Select ... from ... where id IN ("1","2","3","4");
   * </pre>
   */
  public <E extends Record> List<E> recordsQuery(Class<E> clazz, Object... args) {
    E proto = (E) Prototype.proto(clazz);
    SqlSelect sqlSelect;
    Set<String> d_fields = proto.declaredFieldNames();
    String[] fields = new String[d_fields.size()];
    fields = d_fields.toArray(fields);

    //create basic select
    sqlSelect = Sql.select(fields).from(proto.table()).dialect(this.db.dialect);

    if (args.length != 0) {
      if (args.length == 1) {
        sqlSelect.where((String) args[0]);
      } else if (args.length > 2) {
        //1. split args by criterion
        List<List<Object>> arg_groups = new ArrayList<>();
        List<Object> group = new ArrayList<>();
        Object cur;
        Object last;
        for (int i = 0; i < args.length; i++) {
          cur = args[i];
          last = (i - 1) >= 0 ? args[i - 1] : null;
          if (last != null && cur instanceof Criterion) {
            //size > 2 is the valid size
            if (group.size() > 2) {
              //put queryRS group into groups
              //[key,cri,args...,(key),(cri)]
              //remove the redundant key
              group.remove(group.size() - 1);
              arg_groups.add(group);
            }
            //new an array for put
            group = new ArrayList<>();
            //insertRecord key
            group.add(last);
            //insertRecord criterion
            group.add(cur);
          } else {
            group.add(cur);
          }
        }
        arg_groups.add(group);
        //2.make queryRS
        for (List q_group : arg_groups) {
          if (q_group.size() > 2) {
            sqlSelect.where((String) q_group.remove(0),
                            (Criterion) q_group.remove(0),
                            (String[]) q_group.toArray(new String[q_group.size()]));
          }
          //else ignore illegal arguments
        }
      } else {
        throw new RuntimeException(
            "argument length should be >3 or 1 or 0"
        );
      }
    }

    return this.query(proto.mapper(), sqlSelect.tuple());
  }

    /*
     *
     * Using mysql as dialect for now ...
     */

  //TODO add where clause
  public boolean recordExists(Class clazz, String id) {
    Record r = Prototype.proto(clazz);
    String tableName = r.table();
    String pkLbl = r.idName();
    SqlSelect select =
        Sql.select().count().from(tableName).where(pkLbl, Criterion.EQ, id);
    return this.count(select.tuple()) > 0;
  }

  public boolean recordInsert(Record record) {
    List<Tuple<String, Object>> values = new ArrayList<>();
//        for (String f : r.declaredFieldNames()) {
//            Object val = r.get(f);
//            if (val != null) {
//                values.insertRecord(Tuple.t2(f, val));
//            }
//        }
    Map<String, Object> db = record.db();

    _for(db).each(e -> {
      if (e.getValue() != null)
        values.add(Tuple.t2(e.getKey(), e.getValue()));
    });

    String[] keys = _for(values).map(t -> t._a).join();

    //TODO
    SqlInsert sql = Sql.insert().dialect(this.db.dialect);
    sql.into(record.table()).values(ARRAY.of(values));
    S._debug(DB.logger, logger -> logger.debug(sql.debug()));
    try {
      return oper.execute(sql.preparedSql(), sql.params(),
                          getTypes(record.table(), keys)) > 0;
    } catch (SQLException e) {
      throw new RuntimeSQLException(e);
    }
  }

  public boolean recordDelete(Record record) {
    //TODO
    SqlDelete sql = Sql.delete().dialect(this.db.dialect);
    sql.from(record.table())
        .where(record.idName(), Criterion.EQ, (String) record.id());
    S._debug(DB.logger, logger -> logger.debug(sql.debug()));
    try {
      return oper.execute(sql.preparedSql(), sql.params(),
                          new int[]{getType(record.table(), record.idName())}) > 0;
    } catch (SQLException e) {
      throw new RuntimeSQLException(e);
    }
  }


  public boolean recordUpdate(Record record) {
    List<Tuple<String, Object>> sets = new ArrayList<>();

    Map<String, Object> db = record.db();

    _for(db).each(e -> sets.add(Tuple.t2(e.getKey(), e.getValue())));
    String[] keys = _for(sets).map(t -> t._a).join();
    //TODO
    SqlUpdate sql = Sql.update(record.table()).dialect(this.db.dialect);
    sql.set(ARRAY.of(sets)).where(record.idName(), Criterion.EQ, (String) record.id());
    S._debug(DB.logger, logger -> logger.debug(sql.debug()));
    // 多出来的最后一个类型是id
    // types = [types_of_set, $ types_of id]
    int[] types_of_sets = getTypes(record.table(), keys);
    int[] types = new int[types_of_sets.length + 1];

    System.arraycopy(types_of_sets, 0, types, 0, types_of_sets.length);
    //types of id
    types[types.length - 1] = getType(record.table(), record.idName());

    try {
      return oper.execute(sql.preparedSql(), sql.params(), types) > 0;
    } catch (SQLException e) {
      throw new RuntimeSQLException(e);
    }
  }


  @Override
  public void close() throws IOException {
    this.oper.close();
  }

  public Set<String> tables() {
    try {
      return oper.getTableNames();
    } catch (SQLException e) {
      throw new RuntimeSQLException(e);
    }
  }

}
