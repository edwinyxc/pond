package com.shuimin.pond.codec.db;

import com.shuimin.common.f.Holder;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by ed on 2014/4/24.
 */
public interface Record extends Map<String, Object> {

    Holder<String> _tableName = new Holder<>();

    Set<String> _priKeys = new HashSet<>();

    default public Set<String> fields() {
        return this.keySet();
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

    <E> E get(String s);

    Record set(String s, Object val);

    List<Record> innerRecords();

    default void save() {
        DB.fire(DB::getConnFromPool, (tmpl) -> tmpl.add(this));
    }

    default void update() {
        DB.fire(DB::getConnFromPool, (tmpl) -> tmpl.upd(this));
    }

    default void delete() {
        DB.fire(DB::getConnFromPool, (tmpl) -> tmpl.del(this));
    }
}
