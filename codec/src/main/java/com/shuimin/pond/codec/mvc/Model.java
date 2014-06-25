package com.shuimin.pond.codec.mvc;

import com.shuimin.pond.core.db.Record;


/**
 * Created by ed on 5/29/14.
 */
public interface Model extends Record {

    default public Model id(String fieldName) {
        this.field(fieldName);
        this.primaryKeyName(fieldName);
        return this;
    }
}
