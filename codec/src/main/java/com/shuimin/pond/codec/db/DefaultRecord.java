package com.shuimin.pond.codec.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.shuimin.common.S.dump;

/**
 * Created by ed on 2014/4/24.
 */
public class DefaultRecord extends HashMap<String,Object>
    implements Record{

    private List<Record> others = new ArrayList<>();

    @Override
    @SuppressWarnings("unchecked")
    public <E> E get(String s) {
        return (E) super.get(s);
    }

    @Override
    public Record set(String s, Object val) {
        this.put(s, val);
        return this;
    }

    @Override
    public List<Record> innerRecords() {
        return others;
    }

    public DefaultRecord add(Record r) {
        others.add(r);
        return this;
    }


    public Record setInner(String tablename,String colName,Object val) {
        Record record ;
        if(null == (record = getInnerRecord(tablename))) {
            record = new DefaultRecord();
            record.table(tablename);
        }
        record.set(colName,val);
        return this;
    }

    public Record getInnerRecord(String tableName) {
        for(Record r : others) {
            if(tableName.equals(r.table())) {
                return r;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "CompoundRecord{" +
            "_this=" + dump(this) +
            ", others=" + dump(others) +
            ", table=" + this.table() +
            '}';
    }
}
