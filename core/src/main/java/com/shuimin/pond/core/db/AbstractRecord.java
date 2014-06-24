package com.shuimin.pond.core.db;

import com.shuimin.common.S;
import com.shuimin.pond.core.Request;
import com.shuimin.pond.core.kernel.PKernel;
import com.shuimin.pond.core.spi.MultipartRequestResolver;

import java.io.IOException;
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
                return ret;
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
    MultipartRequestResolver requestResolver
            = PKernel.getService(MultipartRequestResolver.class);
    @SuppressWarnings("unchecked")
    @Override
    public Record of(Request req) {
        if(requestResolver.isMultipart(req)){
            try {
                this.merge(requestResolver.resolve(req));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            for (String f : this.fields()) {
                Object o = req.param(f);
                if (!f.equals(priKeyLabel) && o != null)
                    this.set(f, o);
            }
        }
        return this;
    }

    @Override
    public Record merge(Map map) {
        for (String f : this.fields()) {
            Object o = map.get(f);
            if (!f.equals(priKeyLabel) && o != null)
                this.set(f, o);
        }
        return this;
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
