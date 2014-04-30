package com.shuimin.pond.codec.ar.sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.shuimin.common.S._for;

/**
 * Created by ed on 2014/4/30.
 */
public class TSqlUpdate implements SqlUpdate {

    String table;
    List<String> where = new ArrayList<>();
    List<String> fields = new ArrayList<>();

    public TSqlUpdate(String table) {
        this.table = table;
    }

    @Override
    public SqlUpdate set(String... columns) {
        fields.addAll(Arrays.asList(columns));
        return this;
    }

    @Override
    public SqlUpdate where(String... conditions) {
        where.addAll(Arrays.asList(conditions));
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(table)
            .append(" SET ")
            .append(String.join(", ",_for(fields).map( i -> i+" = ?").val()));
        if(! where.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", where));
        }
        return sql.toString();
    }
}
