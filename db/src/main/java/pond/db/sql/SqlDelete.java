package pond.db.sql;

/**
 * Created by ed on 2014/4/30.
 */
public interface SqlDelete extends Sql, SqlWhere {
    public SqlDelete from(String table);
}
