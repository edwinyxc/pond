package com.shuimin.pond.db;

import com.shuimin.common.S;
import com.shuimin.common.f.Callback;
import com.shuimin.common.f.Function;
import com.shuimin.common.f.Tuple;
import com.shuimin.common.sql.Criterion;
import com.shuimin.common.sql.Sql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.shuimin.common.S._for;
import static com.shuimin.common.S._notNullElse;

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

    @SuppressWarnings("unchecked")
    private static RowMapper<AbstractRecord> _default_rm =
            (RowMapper<AbstractRecord>) new AbstractRecord() {
            }.rm;

    static Logger logger = LoggerFactory.getLogger(JDBCTmpl.class);
    final
    Function<Integer, ResultSet> counter = rs -> {
        try {
            return (Integer) rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    };
    protected JDBCOper oper;

    public JDBCTmpl(
            JDBCOper oper) {
        this.oper = oper;
    }

    public List<Record> find(String sql, String... x) {
        return this.map(_default_rm::map, sql, x);
    }

    public List<Record> find(String sql) {
        return this.map(_default_rm::map, sql);
    }

    public <R> List<R> map(
            Class<R> clazz,
            String sql, Object... x
    ) {
        try {
            Record c = S._one(clazz);

            @SuppressWarnings("unchecked")
            Function<R, ResultSet> rm = (rs) -> (R) c.mapper().map(rs);

            return this.map(rm, sql, x);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int count(String sql, Object[] params) {
        return _notNullElse(S.<Integer>_for(map(counter, sql,
                params)).first(), 0);
    }

    public int count(Tuple<String, Object[]> mix) {
        return count(mix._a, mix._b);
    }

// removed page function
//    public <R> Page<R> page(Function<?, ResultSet> mapper,
//                            Page<R> page ) {
//        List<R> r = map(mapper, page.querySql());
//        int count = (Integer) _for(map(counter,page.querySql())).first();
//        return page.fulfill(r, count);
//    }

    public <R> List<R> map(Function<?, ResultSet> mapper,
                           Tuple<String, Object[]> mix) {
        return map(mapper, mix._a, mix._b);
    }

    @SuppressWarnings("unchecked")
    public <R> List<R> map(Function<?, ResultSet> mapper,
                           String sql, Object... x) {

        ResultSet rs;
        List<R> list = new ArrayList<>();
        try {
            rs = oper.query(sql, x);
            while (rs.next()) {
                //check
                list.add((R) mapper.apply(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeSQLException(e);
        }
        return list;
    }

    /**
     * run callback(s) in a transaction
     *
     * @param callback callback(s)
     */
    public void tx(Callback<JDBCTmpl>... callback) {
        try {
            oper.transactionStart();
            _for(callback).each(c -> c.apply(this));
            oper.transactionCommit();
        } catch (Exception e) {
            try {
                oper.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
                throw new RuntimeSQLException(e);
            }
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
        } catch (SQLException e) {
            try {
                oper.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
                throw new RuntimeSQLException(e);
            }
            throw new RuntimeException(e);
        }
    }


    public int exec(Tuple<String, Object[]> sql_t) {
        return exec(sql_t._a, sql_t._b);
    }

    /**
     * execute sql without prepared-statement
     *
     * @param sql
     * @return
     */
    public int exec(String sql) {
        try {
            return oper.execute(sql);
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
            return oper.execute(sql, params);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

    public ResultSet query(String sql) {
        try {
            return oper.query(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeSQLException(e);
        }
    }

    public ResultSet query(String sql, String... x) {
        try {
            return oper.query(sql, x);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeSQLException(e);
        }
    }

    private void cleanComma(StringBuilder sb) {
        if (sb.charAt(sb.length() - 1) == ',') {
            sb.delete(sb.length() - 1, sb.length());
        }
    }


    /*----CURD
    Record#db
    -------*/

    public boolean add(Record record) {
        List<Tuple<String, Object>> values = new ArrayList<>();
//        for (String f : r.declaredFields()) {
//            Object val = r.get(f);
//            if (val != null) {
//                values.add(Tuple.t2(f, val));
//            }
//        }
        _for(record.db()).each(e -> {
            if (e.getValue() != null)
                values.add(Tuple.t2(e.getKey(), e.getValue()));
        });

        Sql sql = Sql.insert().into(record.table()).values(S.array.of(values));
        logger.debug(sql.debug());
        try {
            return oper.execute(sql.preparedSql(), sql.params()) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeSQLException(e);
        }
    }

    public boolean del(Record record) {
        Sql sql = Sql.delete().from(record.table())
                .where(record.idName(), Criterion.EQ, (String) record.id());
        logger.debug(sql.debug());
        try {
            return oper.execute(sql.preparedSql(), sql.params()) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeSQLException(e);
        }
    }


    public boolean upd(Record record) {
        List<Tuple<String, Object>> sets = new ArrayList<>();
        _for(record.db()).each(e -> sets.add(Tuple.t2(e.getKey(), e.getValue())));
        Sql sql = Sql.update(record.table()).set(S.array.of(sets))
                .where(record.idName(), Criterion.EQ, (String) record.id());
        logger.debug(sql.debug());
        try {
            return oper.execute(sql.preparedSql(), sql.params()) > 0;
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
