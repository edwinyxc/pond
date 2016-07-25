package pond.db.sql;

import pond.common.S;
import pond.common.f.Tuple;

import java.util.Arrays;

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

  default T where(Tuple.T3<String, Criterion, Object[]>... conditions) {
    for (Tuple.T3<String, Criterion, Object[]> t : conditions) {
      where(t._a, t._b, (String[]) t._c);
    }
    return (T) this;
  }

  default T where(String key, Criterion c, String... x) {
    AbstractSql sql = (AbstractSql) this;
    sql.where.add(c.prepare(key, x, sql.dialect));
    sql.keyOrder.add(key);
    sql.params.addAll(Arrays.asList(x));
    return (T) sql;
  }


  /**
   * This will not add key to keyOrder since we don't know what the key is.
   *
   * @param where
   * @return
   */
  default T where(String... where) {
    AbstractSql sql = (AbstractSql) this;
    S._tap(Arrays.asList(where), arr -> {
      sql.keyOrder.addAll(arr);
      sql.where.addAll(arr);
    });
    return (T) sql;
  }

}
