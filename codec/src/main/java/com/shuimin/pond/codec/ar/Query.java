package com.shuimin.pond.codec.ar;

import com.shuimin.pond.codec.ar.sql.SqlSelect;

import java.util.List;

import static com.shuimin.common.S._for;

/**
 * Created by ed on 2001/1/1.
 */
public class Query {
    final Table table;
    SqlSelect sql;

    Query(Table table) {
        this.table = table;
    }

    public List<ActiveRecord> all(String... params) {
        return table.query(sql,params);
    }

    public ActiveRecord one(String... params) {
        limit(1);
        return _for(all(params)).first();
    }

    public Query limit(int l) {
        sql.limit(1);
        return this;
    }

    public Query offset(int o) {
        sql.offset(o);
        return this;
    }
}
