package com.shuimin.pond.codec.db;

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

import static com.shuimin.common.S._for;
import static com.shuimin.common.S._notNullElse;
import static com.shuimin.common.S._throw;

/**
 * Created by ed on 2014/4/18.
 */
public class JdbcTmpl implements Closeable {


    protected JdbcOperator oper;

    public JdbcTmpl(
            JdbcOperator oper) {
        this.oper = oper;
    }

    private static RowMapper<AbstractRecord> _default_rm =
            (RowMapper<AbstractRecord>) new AbstractRecord() {
            }.rm;

    public List<Record> find(String sql, String... x) {
        return this.map((rs) -> _default_rm.map(rs), sql, x);
    }

    public List<Record> find(String sql) {
        return this.map((rs) -> _default_rm.map(rs), sql);
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

    final
    Function<Integer, ResultSet> counter = rs -> {
        try {
            return (Integer) rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    };

    public int count(String sql,Object[] params) {
        return _notNullElse(S.<Integer>_for(map(counter, sql,
                params)).first(), 0);
    }

    public int count(Tuple<String,Object[]> mix) {
        return count(mix._a, mix._b);
    }

//    public <R> Page<R> page(Function<?, ResultSet> mapper,
//                            Page<R> page ) {
//        List<R> r = map(mapper, page.querySql());
//        int count = (Integer) _for(map(counter,page.querySql())).first();
//        return page.fulfill(r, count);
//    }

    public <R> List<R> map(Function<?, ResultSet> mapper,
                           Tuple<String,Object[]> mix) {
        return map(mapper,mix._a,mix._b);
    }

    public <R> List<R> map(Function<?, ResultSet> mapper,
                           String sql, Object... x) {

        ResultSet rs = oper.executeQuery(sql, x);
        List<R> list = new ArrayList<>();
        try {
            while (rs.next()) {
                //check
                list.add((R)mapper.apply(rs));
            }
        } catch (SQLException e) {
            _throw(e);
        }
        return list;
    }

    public boolean tx(Callback<JdbcTmpl> callback) {
        try {
            oper.transactionStart();
            callback.apply(this);
            oper.transactionCommit();
            return true;
        } catch (Exception e) {
            oper.rollback();
            throw new UnexpectedException(e);
        }
    }

    public boolean tx(String... batch) {
        try {
            oper.transactionStart();
            for (String sql : batch) {
                ;
                oper.execute(sql);
            }
            oper.transactionCommit();
            return true;
        } catch (SQLException e) {
            oper.rollback();
            throw new UnexpectedException(e);
        }
    }

    public int exec(Tuple<String,Object[]> sql_t) {
        return exec(sql_t._a,sql_t._b);
    }

    public int exec(String sql) {
        try {
            return oper.execute(sql);
        } catch (SQLException e) {
            throw new UnexpectedException(e);
        }
    }

    public int exec(String sql, Object... x) {
        try {
            return oper.execute(sql, x);
        } catch (SQLException e) {
            throw new UnexpectedException(e);
        }
    }

    public void drop(String tbName) {
        exec("DROP TABLE " + tbName);
    }

    public void truncate(String tbName) {
        exec("TRUNCATE TABLE " + tbName);
    }

    public ResultSet query(String sql) {
        return oper.executeQuery(sql);
    }

    public ResultSet query(String sql, String... x) {
        return oper.executeQuery(sql, x);
    }

    public boolean add(Record r) {

        StringBuilder all = new StringBuilder("INSERT INTO ");
        StringBuilder fields = new StringBuilder(" (");

        StringBuilder values = new StringBuilder(" )VALUES(");
        Object[] valuesObjs = new Object[r.fields().size()];

        Holder.AccumulatorInt i = new Holder.AccumulatorInt(0);
        _for(r.fields()).each((f) -> {
            fields.append(f);
            values.append("?");
            if (i.val != r.fields().size() - 1) {
                fields.append(", ");
                values.append(", ");
            }
            valuesObjs[i.accum()] = r.get(f);
        });

        all.append(r.table()).append(" ")
                .append(fields).append(values).append(" ) ");

        String sql = all.toString();

        try {
            oper.execute(sql, valuesObjs);
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

    public Set<String> tables() {
        return oper.getTableNames();
    }

    public boolean upd(Record r) {
        StringBuilder update = new StringBuilder(" UPDATE ");

        StringBuilder set = new StringBuilder(" SET ");

        StringBuilder where = new StringBuilder(" WHERE ");

        int whereCnt = 0;

        List<Object> valuesObjs = new ArrayList<>();

        String pk = r.PK();
        Iterable<String> nonPrimaryFields =
                _for(r.fields()).grep(s -> !s.equals(pk)).val();

        for (Iterator<String> iterator = nonPrimaryFields.iterator();
             iterator.hasNext(); ) {
            String f = iterator.next();
            set.append(f).append(" = ?");
            valuesObjs.add(r.get(f));
            if (iterator.hasNext()) {
                set.append(", ");
            }
        }

        if (pk != null) {
            where.append(pk).append(" = ?");
            whereCnt++;
            valuesObjs.add(r.get(pk));
        }

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


}
