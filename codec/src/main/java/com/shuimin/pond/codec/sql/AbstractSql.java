package com.shuimin.pond.codec.sql;

/**
 * Created by ed on 14-5-22.
 */
public abstract class AbstractSql implements Sql {

    @Override
    public String toString() {
        return preparedSql();
    }

}
