package pond.db.sql.dialect;

/**
 * Created by ed on 12/17/15.
 */
public class H2Dialect implements Dialect {
  @Override
  public String wrapKey(String key) {
    return "\"" + key + "\"";
  }

}
