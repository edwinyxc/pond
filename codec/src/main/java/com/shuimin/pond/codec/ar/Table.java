package com.shuimin.pond.codec.ar;

import com.shuimin.pond.codec.ar.sql.SqlSelect;
import com.shuimin.pond.core.Server;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.shuimin.common.S._for;

/**
 * Created by ed on 2001/1/1.
 */
public class Table {

    private ActiveRecordPlugin config = Server.register(ActiveRecordPlugin.class);

    private final ActiveTmpl dbo;

    private final String name;

    private final Map<String, Class> cols;

    private final String primaryKey;

    public Table(ActiveTmpl dbo, String name, Map<String, Class> cols) {
        this.dbo = dbo;
        this.name = name;
        this.cols = cols;
        this.primaryKey = name.concat(".")
            .concat(config.keyId);
    }

    public Map<String, Class> cols() {
        return Collections.unmodifiableMap(cols);
    }

    public ActiveRecord insert(Object... x) {
        return null;
    }

    public void delete(ActiveRecord activeRecord) {

    }

    public void update(ActiveRecord activeRecord) {

    }

    public void truncate() {
        dbo.truncate(name);
    }

    public List<ActiveRecord> query(SqlSelect sql, String... args) {

        return
            _for(dbo.find(sql.toString(),args)).map(ActiveRecord::new)
            .toList();
    }


}

