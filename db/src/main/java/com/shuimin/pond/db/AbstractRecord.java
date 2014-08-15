package com.shuimin.pond.db;

import com.shuimin.common.S;
import com.shuimin.common.f.Function;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

import static com.shuimin.common.S._for;
import static com.shuimin.common.S.str.underscore;

/**
 * Created by ed on 14-5-19.
 * AbstractRecord
 */
public abstract class AbstractRecord extends HashMap<String, Object>
        implements Record {

    String _tableName = "";
    String priKeyLabel = DEFAULT_PRI_KEY;
    Set<String> declaredFields = new HashSet<String>() {
        {
            add(priKeyLabel);
        }
    };
    /**
     * default map
     * TODO: ugly implement
     */
    RowMapper<?> rm =
            (ResultSet rs) -> {
                Class c = this.getClass();
                AbstractRecord ret = (AbstractRecord) Record.newValue(c);
                try {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int cnt = metaData.getColumnCount();
                    String mainTableName = metaData.getTableName(1);
                    ret.table(mainTableName);
                    for (int i = 0; i < cnt; i++) {
                        String className = metaData.getColumnClassName(i + 1);
                        String retTable = metaData.getTableName(i + 1);
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

                        if (mainTableName.equals(retTable))
                            ret.set(colName, val);
                        else ret.setInner(retTable, colName, val);
                    }
                } catch (SQLException e) {
                    S._lazyThrow(e);
                }
                return ret.init();
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
    @SuppressWarnings("unchecked")
    public String id() {
        return this.get(priKeyLabel);
    }

    @Override
    public String idName() {
        return priKeyLabel;
    }

    @Override
    public Set<String> declaredFields() {
        return declaredFields;
    }

    @Override
    public Set<String> fields() {
        return this.keySet();
    }

    @Override
    public Record setId(Object pk) {
        this.set(priKeyLabel, pk);
        return this;
    }


    @Override
    public void id(String label) {
        declaredFields.remove(priKeyLabel);
        priKeyLabel = label;
        declaredFields.add(priKeyLabel);
    }

    protected Map<String, Function> dbFuncs = new HashMap<>();
    protected Map<String, Function> viewFuncs = new HashMap<>();
    protected Map<String, Function> initFuncs= new HashMap<>();
    {
        //default by class name
        table(underscore(this.getClass().getSimpleName()));
    }

    public class SimpleField implements Field {
        String name;

        public SimpleField(String name) {
            this.name = name;
            AbstractRecord.this.declaredFields().add(name);
            AbstractRecord.this.set(name, null);
        }

        @Override
        public Field init(Function init) {
            AbstractRecord.this.initFuncs.put(name, init);
            return this;
        }

        @Override
        public Field view(Function view) {
            AbstractRecord.this.viewFuncs.put(name, view);
            return this;
        }

        @Override
        public Field db(Function data) {
            AbstractRecord.this.dbFuncs.put(name, data);
            return this;
        }
    }

    @Override
    public Field field(String name) {
        return new SimpleField(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final <E> E view(String s) {
        return viewFuncs.get(s) == null ? get(s) : (E) viewFuncs.get(s).apply(get(s));
    }


    @Override
    public Record merge(Map<String, Object> map) {
        _for(map).each(e -> {
            String name = e.getKey();
            Object value = e.getValue();
            this.set(name, value);
        });
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> E init(String s) {
        Function f = initFuncs.get(s);
        Object ori = this.get(s);
        if (f != null) return (E) f.apply(ori);
        return (E) ori;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final <E> E db(String s) {
        Function f = dbFuncs.get(s);
        Object origin = this.get(s);
        if (f != null) return (E) f.apply(origin);
        return (E) origin;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> E get(String s) {
        return (E) super.get(s);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Record set(String s, Object val) {
        super.put(s, val);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
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
        others.add(record);
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
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || !(o instanceof AbstractRecord)) {
            return false;
        }
        AbstractRecord other = (AbstractRecord) o;
        Object vid = this.id();
        // if the id is missing, return false
        if (vid == null)
            return false;

        // equivalence by id
        return vid.equals(other.id());
    }

    @Override
    public int hashCode() {
        if (id() != null) {
            return id().hashCode();
        } else {
            return super.hashCode();
        }
    }
}
