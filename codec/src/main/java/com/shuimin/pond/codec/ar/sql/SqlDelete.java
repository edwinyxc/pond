package com.shuimin.pond.codec.ar.sql;

/**
 * Created by ed on 2014/4/30.
 */
public interface SqlDelete {
    public SqlDelete from(String table);
    public SqlDelete where(String... conditions);
}