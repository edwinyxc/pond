package com.shuimin.pond.codec.ar.sql;

/**
 * Created by ed on 2014/4/30.
 */
public interface SqlSelect {

    public SqlSelect from(String table);

    public SqlSelect join(String table);

    public SqlSelect on(String... conditions);

    public SqlSelect where(String... conditions);

    public SqlSelect groupBy(String... columns);

    public SqlSelect having(String... conditions);

    public SqlSelect orderBy(String... columns);

    public SqlSelect limit(int limit);

    public SqlSelect offset(int offset);
}
