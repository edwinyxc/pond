package com.shuimin.pond.core.db;

import com.shuimin.common.S;
import com.shuimin.common.f.Callback;
import com.shuimin.common.f.Function;
import com.shuimin.common.f.Holder;
import com.shuimin.common.f.Tuple;
import com.shuimin.pond.core.exception.UnexpectedException;

import java.io.Closeable;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.shuimin.common.S.*;

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

    public List<Record> find(String sql) throws SQLException {
        return this.map(_default_rm::map, sql);
    }

    public <R> List<R> map(
            Class<R> clazz,
            String sql, Object... x
    ) throws SQLException {
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

    public int count(String sql, Object[] params) throws SQLException {
        return _notNullElse(S.<Integer>_for(map(counter, sql,
                params)).first(), 0);
    }

    public int count(Tuple<String, Object[]> mix) throws SQLException {
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
                           Tuple<String, Object[]> mix) throws SQLException {
        return map(mapper, mix._a, mix._b);
    }

    @SuppressWarnings("unchecked")
    public <R> List<R> map(Function<?, ResultSet> mapper,
                           String sql, Object... x) {

        ResultSet rs ;
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
            throw new UnexpectedException(e);
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
            throw new UnexpectedException(e);
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
            throw new UnexpectedException(e);
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
            throw new UnexpectedException(e);
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

    public ResultSet query(String sql) throws SQLException {
        return oper.query(sql);
    }

    public ResultSet query(String sql, String... x) throws SQLException {
        return oper.query(sql, x);
    }

    private void cleanComma(StringBuilder sb) {
        if (sb.charAt(sb.length() - 1) == ',') {
            sb.delete(sb.length() - 1, sb.length());
        }
    }

    public boolean add(Record r) {
        System.out.println(S.dump(r));
        StringBuilder all = new StringBuilder("INSERT INTO ");
        StringBuilder fields = new StringBuilder(" (");

        StringBuilder values = new StringBuilder(" )VALUES(");
        List<Object> valuesObjs = new ArrayList<>();

        Holder.AccumulatorInt i = new Holder.AccumulatorInt(0);
        _for(r.fields()).each((f) -> {
            Object v = r.get(f);
            if (v != null && !"".equals(v)) {
                fields.append(f);
                values.append("?");
                valuesObjs.add(v);
                fields.append(",");
                values.append(",");
            }
        });
        cleanComma(values);
        cleanComma(fields);
        all.append(r.table()).append(" ")
                .append(fields).append(values).append(" ) ");

        String sql = all.toString();

        try {
            oper.execute(sql, valuesObjs.toArray());
            return true;
        } catch (SQLException e) {
            _throw(e);
        }
        return false;
    }

    public boolean del(Record r) {
        if (r == null) return false;

        StringBuilder all = new StringBuilder("DELETE FROM ");

        StringBuilder where = new StringBuilder(" WHERE ");

        Object[] valuesObjs = new Object[r.fields().size()];

        Holder.AccumulatorInt i = new Holder.AccumulatorInt(0);

        _for(r.fields()).each((f) -> {
            where.append(f).append(" = ? ");
            if (i.val != r.fields().size() - 1) {
                where.append("AND ");
            }
            valuesObjs[i.accum()] = r.get(f);
        });

        all.append(r.table()).append(" ")
                .append(where);

        String sql = all.toString();

        try {
            oper.execute(sql, valuesObjs);
            return true;
        } catch (SQLException e) {
            _throw(e);
        }
        return false;
    }

    public Set<String> tables() throws SQLException {
        return oper.getTableNames();
    }

    public boolean upd(Record r) {
        StringBuilder update = new StringBuilder(" UPDATE ");

        StringBuilder set = new StringBuilder(" SET ");

        StringBuilder where = new StringBuilder(" WHERE ");

        int whereCnt = 0;

        List<Object> valuesObjs = new ArrayList<>();

        String pk = r.pk();
        Iterable<String> nonPrimaryFields =
                _for(r.fields()).grep(s -> !s.equals(pk)).val();

        for (Iterator<String> iterator = nonPrimaryFields.iterator();
             iterator.hasNext(); ) {
            String f = iterator.next();
            Object v = r.get(f);
            if (v != null) {
                set.append(f).append(" = ?");
                valuesObjs.add(v);
                set.append(",");
            }
        }

        if (pk != null) {
            where.append(pk).append(" = ?");
            whereCnt++;
            valuesObjs.add(r.get(pk));
        }

        cleanComma(set);

        update.append(r.table()).append(set)
                .append(where);

        String sql = update.toString();

        if (whereCnt == 0) throw new UnexpectedException("UPD SQL : " + sql + "has empty where clause!");


        try {
            oper.execute(sql, valuesObjs.toArray());
            return true;
        } catch (SQLException e) {
            _throw(e);
        }
        return false;
    }

    @Override
    public void close() throws IOException {
        this.oper.close();
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
