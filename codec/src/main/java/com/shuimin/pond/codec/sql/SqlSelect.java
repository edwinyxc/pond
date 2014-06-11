package com.shuimin.pond.codec.sql;

import com.shuimin.common.f.Tuple;

/**
 * Created by ed on 2014/4/30.
 */
public interface SqlSelect extends Sql, SqlWhere<SqlSelect> {

    public SqlSelect from(String table);

    public SqlSelect join(String table);

    public SqlSelect on(String... conditions);

    public SqlSelect groupBy(String... columns);

    public SqlSelect having(Tuple.T3<String, Criterion, Object[]>... conditions);

    public SqlSelect orderBy(String... columns);

    public SqlSelect limit(int limit);

    public SqlSelect offset(int offset);

    public SqlSelect count();

    public SqlSelect copy();

    public SqlSelect fields(String... fields);


}
