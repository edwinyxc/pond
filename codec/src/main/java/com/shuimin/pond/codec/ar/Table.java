package com.shuimin.pond.codec.ar;

import com.shuimin.pond.core.Server;

import java.util.Collections;
import java.util.Map;

/**
 * Created by ed on 2001/1/1.
 */
public class Table {
    private final ActiveTmpl dbo;

    private final String name;

    private final Map<String, Class> cols;

    private final String primaryKey;

    public Table(ActiveTmpl dbo, String name, Map<String, Class> cols) {
        this.dbo = dbo;
        this.name = name;
        this.cols = cols;
        this.primaryKey = name.concat(".")
            .concat(Server.register(ActiveRecordPlugin.class).keyId);
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
}

