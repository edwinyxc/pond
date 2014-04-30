package com.shuimin.pond.codec.db;

import com.shuimin.common.f.Holder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ed on 2014/4/24.
 */
public interface Record{

    Holder<String> _tableName = new Holder<>();

    Set<String> _priKeys = new HashSet<>();

    public Set<String> fields = new HashSet<>();

    default public Set<String> fields() {
        return fields;
    }

    default public Set<String> primaryFields() {
        return _priKeys;
    }

    public default String table() {
        return _tableName.t;
    }

    public default Record table(String s) {
        _tableName.t = s;
        return this;
    }

    Object get(String s);

    Record set(String s, Object val) ;

    List<Record> innerRecords();
}
