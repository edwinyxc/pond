package pond.common.sql.dialect;

import pond.common.f.Function;

/**
 * Created by ed on 2014/5/4.
 */
public interface Dialect {

    public String wrapKey(String key);

    public String primaryKeyMarkOnCreate();

    public static Dialect mysql = new MySQLDialect();
}
