package com.shuimin.pond.codec.sql;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ed on 14-5-22.
 */
public abstract class AbstractSql implements Sql {

    public List<Object> params = new ArrayList<>();

    public List<String> where = new ArrayList<>();

    @Override
    public String toString() {
        return preparedSql();
    }

    @Override
    public Object[] params() {
        return params.toArray();
    }

}
