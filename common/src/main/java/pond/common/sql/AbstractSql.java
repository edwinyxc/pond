package pond.common.sql;

import pond.common.sql.dialect.Dialect;

import java.util.ArrayList;
import java.util.List;

import static pond.common.S._for;

/**
 * Created by ed on 14-5-22.
 */
public abstract class AbstractSql implements Sql {

    public List<Object> params = new ArrayList<>();

    public List<String> where = new ArrayList<>();

    public Dialect dialect = null;

    protected List<String> wrapForDialect(List<String> in){
        return dialect == null ? in:
                _for(in).map(dialect::wrapKey).toList();
    }

    @Override
    public <T> T dialect(Dialect d) {
        this.dialect = d;
        return (T) this;
    }

    @Override
    public String toString() {
        return preparedSql();
    }

    @Override
    public Object[] params() {
        return params.toArray();
    }

}
