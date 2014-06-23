package com.shuimin.common.sql;

import com.shuimin.common.f.Tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.shuimin.common.S._for;

/**
 * Created by ed on 2014/4/30.
 */
public class TSqlInsert extends AbstractSql
        implements SqlInsert {
    private List<String> fields = new ArrayList<>();
    private String table;

    public TSqlInsert() {
    }

    @Override
    public SqlInsert into(String table) {
        this.table = table;
        return this;
    }

    @Override
    public SqlInsert values(Tuple<String, Object>... columns) {
        for (Tuple<String, Object> t : columns) {
            fields.add(t._a);
            params.add(t._b);
        }
        return this;
    }

    @Override
    public SqlInsert values(String... columns) {
        fields.addAll(Arrays.asList(columns));
        return this;
    }

    @Override
    public String preparedSql() {
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(table)
                .append(" (")
                .append(String.join(", ", fields))
                .append(") VALUES (")
                .append(String.join(", ", _for(fields).map(i -> "?").val()))
                .append(")");
        return sql.toString();
    }
}
