package pond.db.sql;

import pond.common.f.Tuple;

/**
 * Created by ed on 2014/4/30.
 */
public interface SqlUpdate extends Sql, SqlWhere {
  public SqlUpdate set(Tuple<String, Object>... sets);

  public SqlUpdate set(String... sets);
}
