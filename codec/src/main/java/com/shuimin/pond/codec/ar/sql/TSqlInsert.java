package com.shuimin.pond.codec.ar.sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.shuimin.common.S._for;

/**
 * Created by ed on 2014/4/30.
 */
public class TSqlInsert implements SqlInsert {
    public TSqlInsert() {
    }

    private List<String> fields = new ArrayList<>();

    private String table;

    @Override
    public SqlInsert into(String table) {
        this.table = table;
        return this;
    }

    @Override
    public SqlInsert values(String... columns) {
        this.fields.addAll(Arrays.asList(columns));
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(table)
            .append(" (")
            .append(String.join(", ",fields))
            .append(") VALUES (")
            .append(String.join(", ",_for(fields).map( i -> "?").val()))
            .append(")");
        return sql.toString();
    }
}
