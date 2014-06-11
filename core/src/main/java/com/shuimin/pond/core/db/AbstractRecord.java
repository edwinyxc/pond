package com.shuimin.pond.core.db;

import com.shuimin.common.S;
import com.shuimin.pond.core.Request;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.shuimin.pond.core.Renderable.dump;

/**
 * Created by ed on 14-5-19.
 * AbstractRecord
 */
public abstract class AbstractRecord extends HashMap<String, Object>
        implements Record {

    String _tableName = "";
    String priKeyLabel = DEFAULT_PRI_KEY;
    /**
     * default map
     * TODO: ugly implement
     */
    RowMapper<?> rm =
            (ResultSet rs) -> {
                try {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int cnt = metaData.getColumnCount();
                    String mainTableName = metaData.getTableName(1);
                    this.table(mainTableName);
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

                        //TODO ugly!
                        if (type.equals(byte[].class)
                                || type.equals(Byte[].class)) {
                            val = rs.getBinaryStream(i + 1);
                        } else {
                            val = JDBCOper.normalizeValue(rs.getObject(i + 1, type));
                        }

//                    S.echo(String.format("NAME : %s ,TYPE : %s", colName,
//                            val == null ? null : val.getClass()));

                        if (mainTableName.equals(thisTable))
                            this.set(colName, val);
                        else this.setInner(thisTable, colName, val);
                    }
                } catch (SQLException e) {
                    S._lazyThrow(e);
                }
                return this;
            };
    private List<Record> others = new ArrayList<>();

    @Override
    public String table() {
        return _tableName;
    }

    @Override
    public Record table(String s) {
        _tableName = s;
        return this;
    }

    @Override
    public String pk() {
        return this.get(priKeyLabel);
    }

    @Override
    public String primaryKeyName() {
        return priKeyLabel;
    }

    @Override
    public Record pk(Object pk) {
        set(priKeyLabel, pk);
        return this;
    }

    @Override
    public void primaryKeyName(String label) {
        priKeyLabel = label;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Record merge(Request req) {
        Record t;
        try {
            Class<?> cls = this.getClass();
            t = (Record) cls.newInstance();
            for (String f : this.fields()) {
                Object o = req.param(f);
                if (o != null) t.set(f, o);
            }
            return t;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            S._throw(e);
        }
        throw new RuntimeException("fail");
    }

    @Override
    public Record merge(Map map) {
        Record t;
        try {
            Class<?> cls = this.getClass();
            t = (Record) cls.newInstance();
            for (String f : this.fields()) {
                Object o = map.get(f);
                if (o != null) t.set(f, o);
            }
            return t;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            S._throw(e);
        }
        throw new RuntimeException("fail");

    }

    @Override
    public RowMapper mapper() {
        return this.rm;
    }

    @Override
    public Record mapper(RowMapper mapper) {
        this.rm = mapper;
        return this;
    }

    public Record setInner(String tablename, String colName, Object val) {
        Record record;
        if (null == (record = getInnerRecord(tablename))) {
            record = new AbstractRecord() {
            };
            record.table(tablename);
        }
        record.set(colName, val);
        return this;
    }

    public Record getInnerRecord(String tableName) {
        for (Record r : others) {
            if (tableName.equals(r.table())) {
                return r;
            }
        }
        return null;
    }


    @Override
    public String toString() {
        return this.getClass().getSimpleName() +
                "{" +
                "_this=" + dump(this) +
                ", others=" + dump(others) +
                ", table=" + this.table() +
                '}';
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> E get(String s) {
        return (E) super.get(s);
    }

    @Override
    public Record set(String s, Object val) {
        super.put(s, val);
        return this;
    }
}
