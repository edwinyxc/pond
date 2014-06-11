package com.shuimin.pond.codec.sql;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ed on 2014/4/30.
 */
public class TSqlDelete extends AbstractSql implements SqlDelete {

    List<String> where = new ArrayList<>();
    List<String> where_params = new ArrayList<>();
    String table;

    public TSqlDelete() {
    }

    @Override
    public SqlDelete from(String table) {
        this.table = table;
        return this;
    }

    @Override
    public String preparedSql() {
        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(table);
        if (!where.isEmpty())
            sql.append(" WHERE ").append(String.join(" AND ", where));
        return sql.toString();
    }

    @Override
    public Object[] params() {
        Object[] ret = new Object[where_params.size()];
        int i = 0;
        for (String s : where_params) {
            ret[i++] = s;
        }
        return ret;
    }
}
