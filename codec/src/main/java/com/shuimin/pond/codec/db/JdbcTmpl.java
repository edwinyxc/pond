package com.shuimin.pond.codec.db;

import com.shuimin.common.f.Callback;
import com.shuimin.common.f.Holder;
import com.shuimin.pond.core.exception.UnexpectedException;

import java.io.Closeable;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.shuimin.common.S._for;
import static com.shuimin.common.S._throw;

/**
 * Created by ed on 2014/4/18.
 */
public class JdbcTmpl implements Closeable {
    RowMapper rm = (rs) -> {
        CompoundRecord ret = new CompoundRecord();
        ResultSetMetaData metaData = rs.getMetaData();
        int cnt = metaData.getColumnCount();
        String mainTableName = metaData.getTableName(1);
        ret.table(mainTableName);
        for (int i = 0; i < cnt; i++) {
            String className = metaData.getColumnClassName(i + 1);
            String thisTable = metaData.getTableName(i + 1);
            Class<?> type = Object.class;
            try {
                type = Class.forName(className);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            String colName = metaData.getColumnName(i + 1);
            Object val;
            if (type.equals(byte[].class)
                || type.equals(Byte[].class)) {
                val = rs.getBinaryStream(i + 1);
            } else {
                val = JdbcOperator.normalizeValue(rs.getObject(i + 1, type));
            }

            if (mainTableName.equals(thisTable))
                ret.set(colName, val);
            else ret.setInner(thisTable, colName, val);
        }
        return ret;
    };


    protected JdbcOperator oper;

    public JdbcTmpl(JdbcOperator oper) {
        this.oper = oper;
    }

    public List<Record> find(String sql) {
        return find(sql, new String[]{});
    }

    /**
     * <pre>
     *     按sql查询
     * </pre>
     *
     * @param sql
     * @return
     */
    public List<Record> find(String sql, String... x) {
        ResultSet rs = oper.executeQuery(sql, x);
        List<Record> list = new ArrayList<>();
        try {
            while (rs.next()) {
                list.add(rm.map(rs));
            }
        } catch (SQLException e) {
            _throw(e);
        }
        return list;
    }

    public boolean tx(Callback<JdbcTmpl> callback) {
        try{
            oper.transactionStart();
            callback.apply(this);
            oper.transactionCommit();
            return true;
        }catch (Exception e){
            oper.rollback();
            throw new UnexpectedException(e);
        }
    }

    public boolean tx(String... batch) {
        try{
            oper.transactionStart();
            for(String sql : batch) {;
                oper.execute(sql);
            }
            oper.transactionCommit();
            return true;
        }catch (SQLException e){
            oper.rollback();
            throw new UnexpectedException(e);
        }
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
        exec("DROP TABLE "+ tbName);
    }

    public void truncate(String tbName) {
        exec("TRUNCATE TABLE "+tbName);
    }

    public ResultSet query(String sql) {
        return oper.executeQuery(sql);
    }

    public ResultSet query(String sql, String... x) {
        return oper.executeQuery(sql,x);
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
            if (i.t != r.fields().size() - 1) {
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
        if(r == null) return false;

        StringBuilder all = new StringBuilder("DELETE FROM ");

        StringBuilder where = new StringBuilder(" WHERE ");

        Object[] valuesObjs = new Object[r.fields().size()];

        Holder.AccumulatorInt i = new Holder.AccumulatorInt(0);

        _for(r.fields()).each((f) -> {
            where.append(f).append(" = ? ");
            if (i.t != r.fields().size() - 1) {
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

    public boolean upd(Record r) {
        //TODO untested
        StringBuilder update = new StringBuilder(" UPDATE ");

        StringBuilder set = new StringBuilder(" SET ");

        StringBuilder where = new StringBuilder(" WHERE ");

        int whereCnt = 0;

        List<Object> valuesObjs = new ArrayList<>();

        Set<String> primaryFields = r.primaryFields();
        Iterable<String> nonPrimaryFields =
            _for(r.fields()).grep(s -> !primaryFields.contains(s)).val();

        for (Iterator<String> iterator = nonPrimaryFields.iterator();
             iterator.hasNext(); ) {
            String f = iterator.next();
            set.append(f).append(" = ?");
            valuesObjs.add(r.get(f));
            if (iterator.hasNext()) {
                set.append(", ");
            }
        }

        for(String f : primaryFields ) {
            where.append(f).append(" = ?");
            whereCnt++;
            valuesObjs.add(r.get(f));
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
