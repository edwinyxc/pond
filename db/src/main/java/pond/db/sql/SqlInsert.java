package pond.db.sql;

import pond.common.f.Tuple;

/**
 * Created by ed on 2014/4/30.
 */
public interface SqlInsert extends Sql {
    public SqlInsert into(String table);

    //test
    public SqlInsert values(Tuple<String, Object>... columns);

    public SqlInsert values(String... columns);
}
