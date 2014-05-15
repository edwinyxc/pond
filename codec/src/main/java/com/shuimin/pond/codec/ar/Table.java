package com.shuimin.pond.codec.ar;

import com.shuimin.pond.codec.sql.SqlSelect;
import com.shuimin.pond.core.Pond;

import java.util.List;
import java.util.Set;

import static com.shuimin.common.S._for;

/**
 * Created by ed on 2001/1/1.
 */
public class Table {

    private ActiveRecordPlugin config = Pond.register(ActiveRecordPlugin.class);

    private final ActiveTmpl dbo;

    private final String name;

    private final String primaryKey;

    private Set<String> cols;

    public Table(ActiveTmpl dbo, String name, Iterable<String> cols) {
        this.dbo = dbo;
        this.name = name;
        this.primaryKey = name.concat(".")
            .concat(config.keyId);
        _for(cols).each(this.cols::add);
    }

    public ActiveRecord create(Object... x) {
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


        return _for(dbo.find(sql.toString(), args)).map(ActiveRecord::new)
            .toList();
    }


}

