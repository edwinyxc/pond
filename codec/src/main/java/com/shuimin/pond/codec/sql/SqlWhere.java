package com.shuimin.pond.codec.sql;

import com.shuimin.common.f.Tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ed on 14-5-23.
 */
public interface SqlWhere<T extends Sql> {

    List<String> where = new ArrayList<>();

    /**
     * workaround for java-corner-problem
     *
     * @return
     */
    default T _this() {
        return (T) this;
    }

    default T where(Iterable<Tuple.T3<String, Criterion, Object[]>> conditions) {
        for (Tuple.T3<String, Criterion, Object[]> t : conditions) {
            where(t._a, t._b, (String[]) t._c);
        }
        return _this();
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
        return _this();
    }

    default T where(String key, Criterion c, String... x) {
        where.add(c.prepare(key, x));
        _this().params.addAll(Arrays.asList(x));
        return _this();
    }

    default T where(String... where) {
        this.where.addAll(Arrays.asList(where));
        return _this();
    }

    default List<String> _getWhere() {
        return where;
    }

}
