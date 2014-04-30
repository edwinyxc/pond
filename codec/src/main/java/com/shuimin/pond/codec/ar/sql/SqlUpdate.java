package com.shuimin.pond.codec.ar.sql;

/**
 * Created by ed on 2014/4/30.
 */
public interface SqlUpdate {
    public SqlUpdate set(String... columns);
    public SqlUpdate where(String... conditions);
}
