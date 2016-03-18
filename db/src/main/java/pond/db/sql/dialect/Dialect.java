package pond.db.sql.dialect;

/**
 * Created by ed on 2014/5/4.
 */
public interface Dialect {

  public String wrapKey(String key);
//  public String countFlagInSingleStatement();

//  public String primaryKeyMarkOnCreate();

  final public static Dialect mysql = new MySQLDialect();
  final public static Dialect h2 = new H2Dialect();
}
