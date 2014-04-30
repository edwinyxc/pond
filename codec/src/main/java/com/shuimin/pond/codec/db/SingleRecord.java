package com.shuimin.pond.codec.db;


import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>数据记录抽象</p>
 */
public class SingleRecord
    implements Record {

    private Map<String,Object> _this = new HashMap<>();

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
        return Collections.emptyList();
    }

}
