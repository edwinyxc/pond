package com.shuimin.pond.codec.sql;

import com.shuimin.common.f.Tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ed on 14-5-23.
 */
public interface SqlWhere<T extends Sql> {


    default T where(Iterable<Tuple.T3<String, Criterion, Object[]>> conditions) {
        for (Tuple.T3<String, Criterion, Object[]> t : conditions) {
            where(t._a, t._b, (String[]) t._c);
        }
        return (T) this;
    }

    default T where(String key, Tuple<Criterion, Object[]> condition) {
        return where(key, condition._a, (String[]) condition._b);
    }

    default T where(String key, String criterion, String... x) {
        return where(key, Criterion.of(criterion), x);
    }

    default T where(Tuple.T3<String, Criterion, Object[]>... conditions) {
        for (Tuple.T3<String, Criterion, Object[]> t : conditions) {
            where(t._a, t._b, (String[]) t._c);
        }
        return (T) this;
    }

    default T where(String key, Criterion c, String... x) {
        AbstractSql sql = (AbstractSql) this;
        sql.where.add(c.prepare(key, x));
        sql.params.addAll(Arrays.asList(x));
        return (T)sql;
    }

    default T where(String... where) {
        AbstractSql sql = (AbstractSql) this;
        sql.where.addAll(Arrays.asList(where));
        return (T)sql;
    }

}
