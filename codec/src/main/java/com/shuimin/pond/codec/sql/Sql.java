package com.shuimin.pond.codec.sql;

/**
 * Created by ed on 2014/4/28.
 */
public interface Sql {

    public static SqlInsert insert(){
        return new TSqlInsert();
    }

    public static SqlUpdate update(String table){
        return new TSqlUpdate(table);
    }

    public static SqlSelect select(String... cols) {
        return new TSqlSelect(cols);
    }

    public static SqlDelete delete(){
        return new TSqlDelete();
    }

}
