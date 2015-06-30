package pond.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.S;
import pond.common.f.Callback;
import pond.common.f.Function;
import pond.common.f.Tuple;
import pond.common.sql.*;
import pond.common.sql.dialect.Dialect;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

import static pond.common.S._for;
import static pond.common.S._notNullElse;

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

    static Logger logger = LoggerFactory.getLogger(JDBCTmpl.class);

    //database structure
    //table_name -> Map<field_name, field_type(java.sql.Type)>
    Map<String, Map<String, Integer>> dbStructure;

    private Object getObjectAsDefault(ResultSet rs, String table, String field) {
        Map<String, Integer> struc_table = dbStructure.get(table);
        if (struc_table == null)
            throw new RuntimeException("table:"
                    + table + " doesn't exist. We assume the DB structure is immutable.");
        Integer type = struc_table.get(field);
        if (type == null)
            throw new RuntimeException("field:" + field + " doesn't exist. We assume the DB structure is immutable.");
        Function.F2<?, ResultSet, String> field_fetch_method = this.rule.getMethod(type);
        if(field_fetch_method == null)
            throw new RuntimeException("method fetch failed type:" + type );
        return field_fetch_method.apply(rs,field);
    }

    static final
    Function<Integer, ResultSet> counter = rs -> {
        try {
            return (Integer) rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    };

    private JDBCOper oper;
    private MappingRule rule;

    public JDBCTmpl(
            Map<String, Map<String, Integer>> structure,
            MappingRule mappingRule) {
        this.dbStructure = structure;
        this.rule = mappingRule;
    }

    public JDBCTmpl open(JDBCOper oper) {
        this.oper = oper;
        return this;
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

    /**
     * default query
     * TODO: ugly implement
     */
    final Function<AbstractRecord, ResultSet> _default_rm =

            (ResultSet rs) -> {
                AbstractRecord ret = Record.newValue(AbstractRecord.class);
                try {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int cnt = metaData.getColumnCount();

//                    String mainTableName = metaData.getTableName(1);
//
//                    if (S.str.notBlank(mainTableName))
//                        ret.table(mainTableName);

                    String field_name;
                    int field_type;
                    int field_idx;
                    for (int i = 0; i < cnt; i++) {
                        field_idx = i + 1;
                        field_name = metaData.getColumnName(field_idx);
                        field_type = metaData.getColumnType(field_idx);
//                        // if it is a binary-stream
//                        if (type.equals(byte[].class)
//                                || type.equals(Byte[].class)) {
//                            val = rs.getBinaryStream(i + 1);
//                        } else {
//                            //TODO
//                            /*
//                             * if i can use the db type to re-construct the "getObject" method
//                             * im going to create a sub-layer
//                             */
//
//                            val = JDBCOper.normalizeValue(rs.getObject(i + 1, type));
//                        }
//
////                    S.echo(String.format("NAME : %s ,TYPE : %s", colName,
////                            val == null ? null : val.getClass()));
//
//                        if (S.str.notBlank(retTable) && !retTable.equals(mainTableName))
//                            ret.setInner(retTable, colName, val);
//                        else
//                            ret.set(colName, val);

                        ret.set(field_name, rule.getMethod(field_type).apply(rs,field_name));
                    }

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                return ret;
            };

    public List<Record> query(String sql, Object... x) {
        return this.query(_default_rm, sql, x);
    }


    public List<Record> query(String sql) {
        return this.query(_default_rm, sql);
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
        return _notNullElse(S.<Integer>_for(query(counter, sql,
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
        S._try(() -> {
            long start = S.time();
            oper.query(sql, args, rs -> {
                S.echo("####rsTime: " + (S.time() - start));
                S._try(() -> {
                    while (rs.next()) {
                        //check
                        list.add((R) mapper.apply(rs));
                    }
                });
                S.echo("####queryTime: " + (S.time() - start));
            });
        });
        return list;
    }

    /**
     * run callback(s) in a transaction
     *
     * @param callback callback(s)
     */
    public void tx(Callback<JDBCTmpl>... callback) {
        try {
            synchronized (oper) {
                oper.transactionStart();
                _for(callback).each(c -> c.apply(this));
                oper.transactionCommit();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * run batch(s) in a transaction
     *
     * @param batch sql(s)
     */
    public void tx(String... batch) {
        try {
            oper.transactionStart();
            for (String sql : batch) {
                oper.execute(sql);
            }
            oper.transactionCommit();
        } catch (Throwable e) {
            logger.info("Caught throwable [ " + e.getMessage() + " ] IN TRANSACTION, STOP COMMITTING!");
            throw new RuntimeException(e);
        }
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

    /**
     * //TODO: untested
     *
     * @param tbName table name
     */
    public void drop(String tbName) {
        exec("DROP TABLE " + tbName);
    }

    /**
     * //TODO: untested
     *
     * @param tbName table name
     */
    public void truncate(String tbName) {
        exec("TRUNCATE TABLE " + tbName);
    }

    public void queryRS(String sql, Callback<ResultSet> doWithResultSet) {
        queryRS(sql, new String[0], doWithResultSet);
    }

    public void queryRS(String sql, Object[] args, Callback<ResultSet> doWithResultSet) {
        try {
            oper.query(sql, args, doWithResultSet);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
//    public ResultSet queryRS(String sql, String... x) {
//        try {
//            return oper.query(sql, x);
//        } catch (SQLException e) {
//            e.printStackTrace();
//            throw new RuntimeSQLException(e);
//        }
//    }


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
        SqlInsert sql = Sql.insert().dialect(Dialect.mysql);
        sql.into(record.table()).values(S.array.of(values));
        logger.debug(sql.debug());
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
        SqlDelete sql = Sql.delete().dialect(Dialect.mysql);
        sql.from(record.table())
                .where(record.idName(), Criterion.EQ, (String) record.id());
        logger.debug(sql.debug());
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
        SqlUpdate sql = Sql.update(record.table()).dialect(Dialect.mysql);
        sql.set(S.array.of(sets))
                .where(record.idName(), Criterion.EQ, (String) record.id());
        logger.debug(sql.debug());
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

    /**
     * in case ...
     *
     * @return oper
     */
    @Deprecated
    public JDBCOper oper() {
        return this.oper;
    }

}
