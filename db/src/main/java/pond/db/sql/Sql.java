package pond.db.sql;

import pond.common.f.Tuple;
import pond.db.sql.dialect.Dialect;

import static pond.common.S._notNullElse;
import static pond.common.S.avoidNull;

/**
 * Created by ed on 2014/4/28.
 */
public interface Sql {

    public <T> T dialect(Dialect d);

    public static SqlInsert insert() {
        return new TSqlInsert();
    }

    public static SqlUpdate update(String table) {
        return new TSqlUpdate(table);
    }

    public static SqlSelect select(String... cols) {
        return new TSqlSelect(cols);
    }

    public static SqlDelete delete() {
        return new TSqlDelete();
    }

    public String preparedSql();

    public Object[] params();

    default public String debug() {
        Object[] p = params();
        String[] _debug = new String[p.length];
        for(int i =0; i< p.length; i++){
            _debug[i] = avoidNull(p[i], "").toString();
        }
        return String.format("{ sql: %s, params: [ %s ]}",
                preparedSql(),
                String.join(",",_debug)
        );
    }

    default public Tuple<String, Object[]>
    tuple() {
        return Tuple.t2(preparedSql(), params());
    }


}
