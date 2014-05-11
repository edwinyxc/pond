package com.shuimin.pond.codec.sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ed on 2014/4/30.
 */
public class TSqlDelete implements  SqlDelete{

    List<String> where = new ArrayList<>();

    String table;
    public TSqlDelete(){}

    @Override
    public SqlDelete from(String table) {
        this.table = table;
        return this;
    }

    @Override
    public SqlDelete where(String... conditions) {
        where.addAll(Arrays.asList(conditions));
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(table);
        if(! where.isEmpty())
            sql.append(" WHERE ").append(String.join(" AND ", where));
        return sql.toString();
    }
}
