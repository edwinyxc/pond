package pond.db.sql;

import pond.db.Prototype;
import pond.db.Record;

/**
 * Created by ed on 2014/4/30.
 */
public interface SqlDelete extends Sql, SqlWhere<SqlDelete> {
  public SqlDelete from(String table);
  default public SqlDelete from(Class<? extends Record> cls) {
    return from(Prototype.proto(cls).table());
  }
}
