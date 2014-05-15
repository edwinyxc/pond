package com.shuimin.pond.codec.sql;

/**
 * Created by ed on 2014/4/30.
 */
public interface SqlDelete extends Sql{
    public SqlDelete from(String table);
    public SqlDelete where(String... conditions);
}
