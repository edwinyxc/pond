package com.shuimin.pond.codec.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.shuimin.common.S.dump;

/**
 * Created by ed on 2014/4/24.
 */
public class CompoundRecord implements Record{

    private Map<String,Object> _this = new HashMap<>();

    private List<Record> others = new ArrayList<>();

    @Override
    public Object get(String s) {
        return _this.get(s) ;
    }

    @Override
    public Record set(String s, Object val) {
        fields.add(s);
        _this.put(s,val);
        return this;
    }

    @Override
    public List<Record> innerRecords() {
        return others;
    }

    public CompoundRecord add(Record r) {
        others.add(r);
        return this;
    }


    public Record setInner(String tablname,String colName,Object val) {
        Record record ;
        if(null == (record = getInnerRecord(tablname))) {
            record = new CompoundRecord();
            record.table(tablname);
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
            "_this=" + dump(_this) +
            ", others=" + dump(others) +
            ", table=" + this.table() +
            '}';
    }
}
