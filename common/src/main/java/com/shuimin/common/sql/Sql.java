package com.shuimin.common.sql;

import com.shuimin.common.f.Tuple;

import static com.shuimin.common.S._for;

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
        return String.format("{ sql: %s, params: [ %s ]}",
                preparedSql(),
                String.join(",", _for(params()).map(
                        o -> o.toString()).join())
        );
    }

    default public Tuple<String, Object[]>
    tuple() {
        return Tuple.t2(preparedSql(), params());
    }

}
