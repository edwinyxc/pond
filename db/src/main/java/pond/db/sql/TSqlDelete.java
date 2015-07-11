package pond.db.sql;

/**
 * Created by ed on 2014/4/30.
 */
public class TSqlDelete extends AbstractSql implements SqlDelete {

    String table;

    public TSqlDelete() {
    }

    @Override
    public SqlDelete from(String table) {
        this.table = table;
        return this;
    }

    @Override
    public String preparedSql() {
        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(table);
        if (!where.isEmpty())
            sql.append(" WHERE ").append(String.join(" AND ", where));
        return sql.toString();
    }

}
