package com.shuimin.pond.codec.sql;

import com.shuimin.common.f.Tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ed on 2014/4/30.
 */
public class TSqlSelect extends AbstractSql
        implements SqlSelect {
    List<String> tables = new ArrayList<>();
    List<String> fields = new ArrayList<>();
    List<String> groups = new ArrayList<>();
    List<String> having = new ArrayList<>();
    List<String> orders = new ArrayList<>();

    int limit = -1;
    int offset = -1;

    public TSqlSelect(String... columns) {
        if (columns != null && columns.length > 0) {
            this.fields.addAll(Arrays.asList(columns));
        } else {
            this.fields.add("*");
        }
    }

    @Override
    public SqlSelect from(String table) {
        this.tables.add(table);
        return this;
    }

    @Override
    public SqlSelect join(String table) {
        this.tables.add(table);
        return this;
    }

    @Override
    public SqlSelect on(String... where) {
        String table = tables.remove(tables.size() - 1);
        tables.add(table.concat(" ON ").concat(
                String.join(" AND ", Arrays.asList(where))
        ));
        return this;
    }

    @Override
    public SqlSelect groupBy(String... columns) {
        groups.addAll(Arrays.asList(columns));
        return this;
    }

    @Override
    public SqlSelect having(Tuple.T3<String, Criterion, Object[]>... conditions) {
        for (Tuple.T3<String, Criterion, Object[]> t : conditions) {
            having.add(t._b.prepare(t._a, t._c));
            params.addAll(Arrays.asList(t._c));
        }
        return this;
    }

    @Override
    public SqlSelect orderBy(String... columns) {
        orders.addAll(Arrays.asList(columns));
        return this;
    }

    @Override
    public SqlSelect limit(int limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public SqlSelect offset(int offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public SqlSelect count() {
        fields.clear();
        fields.add("count(*)");
        return this;
    }

    @Override
    public SqlSelect copy() {
        TSqlSelect copy = new TSqlSelect();
        copy.fields.addAll(this.fields);
        copy.groups.addAll(this.groups);
        copy.tables.addAll(this.tables);
        copy.having.addAll(this.having);
        copy.orders.addAll(this.orders);
        copy.where.addAll(this.where);
        copy.limit = this.limit;
        copy.offset = this.offset;
        return copy;
    }

    @Override
    public SqlSelect fields(String... fields) {
        this.fields.addAll(Arrays.asList(fields));
        return this;
    }

    @Override
    public String preparedSql() {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(String.join(", ", fields))
                .append(" FROM ")
                .append(String.join(" JOIN ", tables));
        if (!where.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", where));
        }
        if (!groups.isEmpty()) {
            sql.append(" GROUP BY ").append(String.join(", ", groups));
            if (!having.isEmpty()) {
                sql.append(" HAVING ").append(String.join(" AND ", having));
            }
        }
        if (!orders.isEmpty()) {
            sql.append(" ORDER BY ").append(String.join(", ", orders));
        }
        if (limit > 0) {
            sql.append(" LIMIT ").append(String.valueOf(limit));
        }
        if (offset > -1) {
            sql.append(" OFFSET ").append(String.valueOf(offset));
        }
        return sql.toString();
    }


}
