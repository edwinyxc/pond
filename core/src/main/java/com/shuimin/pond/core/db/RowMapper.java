package com.shuimin.pond.core.db;

import java.sql.ResultSet;

/**
 * Created by ed on 2014/4/18.
 */
public interface RowMapper<E> {
    public E map(ResultSet rs);
}