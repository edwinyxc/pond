package pond.db.sql;

import pond.common.f.Tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static pond.common.S._for;

/**
 * Created by ed on 2014/4/30.
 */
public class TSqlUpdate extends AbstractSql
        implements SqlUpdate {

    String table;
    List<String> fields = new ArrayList<>();

    public TSqlUpdate(String table) {
        this.table = table;
    }

    @Override
    public SqlUpdate set(Tuple<String, Object>... columns) {
        for (Tuple<String, Object> t : columns) {
            fields.add(t._a);
            params.add(t._b);
        }
        return this;
    }

    @Override
    public SqlUpdate set(String... sets) {
        fields.addAll(Arrays.asList(sets));
        return this;
    }

    @Override
    public String preparedSql() {
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(table)
                .append(" SET ")
                .append(String.join(", ", _for(wrapForDialect(fields)).map(i -> i + " = ?").val()));
        if (!where.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", where));
        }
        return sql.toString();
    }
}
