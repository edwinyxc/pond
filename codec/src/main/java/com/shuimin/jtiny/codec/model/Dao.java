package com.shuimin.jtiny.codec.model;

import java.util.List;

/**
 * Created by ed on 2014/4/18.
 */
public interface Dao<T extends Model> {
    public T get(String id);
    public List<T> find(Object o);
}
