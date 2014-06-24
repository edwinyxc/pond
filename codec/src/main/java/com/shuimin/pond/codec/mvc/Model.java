package com.shuimin.pond.codec.mvc;

import com.shuimin.common.f.Function;
import com.shuimin.pond.core.db.Record;

import java.util.Collections;
import java.util.Map;

/**
 * Created by ed on 5/29/14.
 */
public interface Model extends Record {

    public interface Field<E>{
        Field init(Function.F0<E> supplier);
        Field onGet(Function<E,E> get);
        Field onSet(Function<E,E> set);
    }

    /**
     * define field
     *
     * @param name field name
     * @return this
     */
    <E> Field<E> field(String name);

    /**
     * get value
     *
     * @return value Object - immutable map
     */
    default public Map<String, Object> val() {
        return Collections.unmodifiableMap(this);
    }

    /**
     * set value
     *
     * @param vo value object
     * @return this
     */
    default public Model val(Map<String, Object> vo) {
        this.clear();
        this.putAll(vo);
        return this;
    }

    default public Model id(String fieldName) {
        this.field(fieldName);
        this.primaryKeyName(fieldName);
        return this;
    }
}
