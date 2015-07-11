package pond.db;

import pond.common.S;
import pond.common.f.Callback;
import pond.common.f.Function;
import pond.common.f.Tuple;
import pond.db.sql.*;
import pond.db.sql.dialect.Dialect;

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
    Map<String, Map<String, Integer>> dbStructure;

    private static final
    Function<Integer, ResultSet> counter = rs -> {
        try {
            return (Integer) rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    };

    private final JDBCOper oper;
    private final DB db;

    JDBCTmpl(DB db, Connection connection) {
        this.db = db;
        this.oper = new JDBCOper(connection);
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
        return dbStructure.getOrDefault(table, Collections.emptyMap()).get(field);
    }


    public List<Record> query(String sql, Object... x) {
        return this.query(db._default_rm, sql, x);
    }


    public List<Record> query(String sql) {
        return this.query(db._default_rm, sql);
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
        R proto = Proto.proto(clazz);
        return query(proto, sql, x);
    }

    public int count(String sql, Object[] params) {
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
//        int count = (Integer) _for(query(counter,page.querySql())).first();
//        return page.fulfill(r, count);
//    }

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
                    S._debug(DB.logger, log -> log.debug("time cost for creating resultSet: " + (S.now() - start)));
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


    /*----CURD
    Record#db
    -------*/

    /*
     *
     * Using mysql as dialect for now ...
     */

    public boolean add(Record record) {
        List<Tuple<String, Object>> values = new ArrayList<>();
//        for (String f : r.declaredFieldNames()) {
//            Object val = r.get(f);
//            if (val != null) {
//                values.add(Tuple.t2(f, val));
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
        sql.into(record.table()).values(S.array.of(values));
        S._debug(DB.logger, logger -> logger.debug(sql.debug()));
        try {
            return oper.execute(sql.preparedSql(), sql.params(),
                    getTypes(record.table(), keys)) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeSQLException(e);
        }
    }

    public boolean del(Record record) {
        //TODO
        SqlDelete sql = Sql.delete().dialect(this.db.dialect);
        sql.from(record.table())
                .where(record.idName(), Criterion.EQ, (String) record.id());
        S._debug(DB.logger, logger -> logger.debug(sql.debug()));
        try {
            return oper.execute(sql.preparedSql(), sql.params(),
                    new int[]{getType(record.table(), record.idName())}) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeSQLException(e);
        }
    }


    public boolean upd(Record record) {
        List<Tuple<String, Object>> sets = new ArrayList<>();

        Map<String, Object> db = record.db();

        _for(db).each(e -> sets.add(Tuple.t2(e.getKey(), e.getValue())));
        String[] keys = _for(sets).map(t -> t._a).join();
        //TODO
        SqlUpdate sql = Sql.update(record.table()).dialect(this.db.dialect);
        sql.set(S.array.of(sets))
                .where(record.idName(), Criterion.EQ, (String) record.id());
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
            e.printStackTrace();
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
            e.printStackTrace();
            throw new RuntimeSQLException(e);
        }
    }

}
