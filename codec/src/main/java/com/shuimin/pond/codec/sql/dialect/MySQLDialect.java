package com.shuimin.pond.codec.sql.dialect;

/**
 * Created by ed on 2014/5/4.
 */
public class MySQLDialect implements Dialect{

    @Override
    public String primaryKeyMarkOnCreate() {
        return "varchar(64) primary key";
    }

}