package com.shuimin.pond.codec.sql;

/**
 * Created by ed on 2014/4/30.
 */
public interface SqlUpdate extends Sql{
    public SqlUpdate set(String... columns);
    public SqlUpdate where(String... conditions);
}
