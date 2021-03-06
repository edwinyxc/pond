package pond.db.sql;

import pond.common.f.Tuple;

/**
 * Created by ed on 2014/4/30.
 */
public interface SqlUpdate extends Sql, SqlWhere<SqlUpdate> {
  public SqlUpdate set(Tuple<String, Object>... sets);

  public SqlUpdate set(String... sets);

  public SqlUpdate set(String name, Object value);
}
