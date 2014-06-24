package com.shuimin.pond.codec.mvc;

import com.shuimin.common.f.Function;
import com.shuimin.pond.core.db.Record;

import java.util.Collections;
import java.util.Map;

/**
 * Created by ed on 5/29/14.
 */
public interface Model extends Record {

    /**
     * define field with default
     *
     * @param name     field name
     * @param supplier supplier
     * @return this
     */
    Model field(String name, Function.F0 supplier);


    /**
     * define field
     *
     * @param name field name
     * @return this
     */
    Model field(String name);

    /**
     * set setter
     *
     * @param name  key
     * @param onSet setter function
     * @return this
     */
    public Model onSet(String name, Function onSet);

    /**
     * set getter
     *
     * @param name  key
     * @param onGet getter function
     * @return this
     */
    public Model onGet(String name, Function onGet);

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
