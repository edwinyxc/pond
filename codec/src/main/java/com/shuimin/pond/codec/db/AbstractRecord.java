package com.shuimin.pond.codec.db;

import com.shuimin.common.S;
import com.shuimin.common.f.Function;
import com.shuimin.pond.core.Request;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

import static com.shuimin.common.S._for;
import static com.shuimin.pond.core.Renderable.dump;

/**
 * Created by ed on 14-5-19.
 */
public abstract class AbstractRecord extends HashMap<String, Object>
        implements Record {

    protected String[] _fields() {
        return new String[0];
    }

    protected AbstractRecord() {
        String[] fields = _fields();
        if (fields == null) fields = new String[0];
        _for(fields).each(f -> this.put(f, this.getDefault(f)));
    }

    protected Map<String, Function.F0> defaults = new HashMap<>();

    protected Map<String, Function> getters = new HashMap<>();

    protected Map<String, Function> setters = new HashMap<>();

    protected Function.F0 getDefault(String s) {
        return defaults.get(s);
    }

    protected void setDefault(String s, Function.F0 provider) {
        this.defaults.put(s, provider);
    }

    protected void onSet(String s, Function<?, ?> converter) {
        this.setters.put(s, converter);
    }

    protected void onGet(String s, Function<?, ?> f) {
        this.getters.put(s, f);
    }

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
    public String PK() {
        return this.get(priKeyLabel);
    }

    @Override
    public String PKLabel() {
        return priKeyLabel;
    }

    @Override
    public Record PK(Object pk) {
        set(priKeyLabel, pk);
        return this;
    }

    @Override
    public void PKLabel(String label) {
        priKeyLabel = label;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Record merge(Request req) {
        Record t = null;
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
        Record t = null;
        try {
            Class<?> cls =this.getClass();
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

    private List<Record> others = new ArrayList<>();

    String _tableName = "";
    String priKeyLabel = DEFAULT_PRI_KEY;

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
                            val = JdbcOperator.normalizeValue(rs.getObject(i + 1, type));
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

    @Override
    public RowMapper mapper() {
        return this.rm;
    }

    @Override
    public Record mapper(RowMapper mapper) {
        this.rm = mapper;
        return this;
    }

    @Override
    public List<Record> innerRecords() {
        return others;
    }

    @Override
    public Record add(Record r) {
        others.add(r);
        return this;
    }

    @Override
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

    @Override
    public Record getInnerRecord(String tableName) {
        for (Record r : others) {
            if (tableName.equals(r.table())) {
                return r;
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> E get(String s) {
        Function getter = getters.get(s);
        Object t = super.get(s);
        return getter != null ? (E) getter.apply(t) : (E) t;
    }

    @Override
    public Record set(String s, Object val) {
        Function setter = setters.get(s);
        if (setter != null)
            this.put(s, setter.apply(val));
        else
            this.put(s, val);
        return this;
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

}
