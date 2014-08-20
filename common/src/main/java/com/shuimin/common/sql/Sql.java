package com.shuimin.common.sql;

import com.shuimin.common.f.Tuple;

import static com.shuimin.common.S._notNullElse;

/**
 * Created by ed on 2014/4/28.
 */
public interface Sql {


    public static SqlInsert insert() {
        return new TSqlInsert();
    }

    public static SqlUpdate update(String table) {
        return new TSqlUpdate(table);
    }

    public static SqlSelect select(String... cols) {
        return new TSqlSelect(cols);
    }

    public static SqlDelete delete() {
        return new TSqlDelete();
    }

    public String preparedSql();

    public Object[] params();

    default public String debug() {
        Object[] p = params();
        String[] _debug = new String[p.length];
        for(int i =0; i< p.length; i++){
            _debug[i] = _notNullElse(p[i],"").toString();
        }
        return String.format("{ sql: %s, params: [ %s ]}",
                preparedSql(),
                String.join(",",_debug)
        );
    }

    default public Tuple<String, Object[]>
    tuple() {
        return Tuple.t2(preparedSql(), params());
    }

}
