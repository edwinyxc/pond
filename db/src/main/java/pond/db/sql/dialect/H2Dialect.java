package pond.db.sql.dialect;

import pond.db.DB;
import pond.db.sql.Sql;

/**
 * Created by ed on 12/17/15.
 */
public class H2Dialect implements Dialect {
  @Override
  public String wrapKey(String key) {
    return "\"" + key + "\"";
  }

  public String countFlagInSingleStatement() {
    Sql.logger.warn("countFlagInSingleStatement is not supported by H2 Database");
    return "";
  }

}
