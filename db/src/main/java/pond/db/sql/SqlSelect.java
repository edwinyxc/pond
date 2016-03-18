package pond.db.sql;

import pond.common.Convert;
import pond.common.S;
import pond.common.STRING;
import pond.common.f.Tuple;
import pond.db.Prototype;
import pond.db.Record;

import java.util.*;

/**
 * Created by ed on 2014/4/30.
 */
public interface SqlSelect extends Sql, SqlWhere<SqlSelect> {

  public SqlSelect from(String table);

  public SqlSelect from(Class<? extends Record> recordClass);

  public SqlSelect join(String table);

  public SqlSelect on(String... conditions);

  public SqlSelect groupBy(String... columns);

  public SqlSelect having(Tuple.T3<String, Criterion, Object[]>... conditions);

  public SqlSelect orderBy(String... columns);

  public SqlSelect limit(int limit);

  public SqlSelect offset(int offset);

  public SqlSelect count();

  public SqlSelect copy();

  public SqlSelect fields(String... fields);

  //mysql SQL_CALC_FOUND_ROWS
//  default public SqlSelect enableCountInSingleSql() {
//    Dialect d = ((AbstractSql) this).dialect;
//    List<String> fields = ((TSqlSelect) this).fields;
//    fields.add(0, d.countFlagInSingleStatement());
//  }

  static final String SORT_IN_URL = S.avoidNull(S.config.get(Sql.class, "sort"), "sort");
  static final String FIELDS_IN_URL = S.avoidNull(S.config.get(Sql.class, "fields"), "fields");
  static final String LIMIT_IN_URL = S.avoidNull(S.config.get(Sql.class, "limit"), "limit");
  static final String OFFSET_IN_URL = S.avoidNull(S.config.get(Sql.class, "offset"), "offset");

  @SuppressWarnings("all")
  default SqlSelect paginate(Map<String, Object> req) {
    String _offset, _limit;
    _offset = (String) req.get(OFFSET_IN_URL);
    _limit = (String) req.get(LIMIT_IN_URL);

    if(STRING.notBlank(_offset) && STRING.notBlank(_limit)){
      this.offset(Convert.toInt(_offset)).limit(Convert.toInt(_limit));
    }

    return this;
  }

  default SqlSelect fields(Map<String, Object> req) {
    String fields = (String) req.get(FIELDS_IN_URL);
    if (fields == null) return this;
    //clear all the fields
    ((TSqlSelect) this).fields.clear();
    return this.fields(fields.split(","));
  }

  default SqlSelect sort(Map<String, Object> req) {
    String sord_f = (String) req.get(SORT_IN_URL);// &_sordf=+a  &_sordf=+a,-b

    if (sord_f == null) return this;

    String[] orders = sord_f.split(",");

    for (String o : orders) {
      if (o != null && o.length() > 1) {
        boolean desc = (o.charAt(0) == '-');
        this.orderBy(o.substring(1) + " " + (desc ? "desc" : "asc"));
      }
    }
    return this;
  }


  default SqlSelect filter(Map<String, Object> req, Class<? extends Record> cls) {
    return filter(req, Prototype.proto(cls));
  }

  default SqlSelect filter(Map<String, Object> req, Record proto) {

    List<Tuple.T3<String, Criterion, Object[]>> conditions = new ArrayList<>();
    for (String f : proto.declaredFieldNames()) {
      String ori_c_and_v = (String) req.getOrDefault(f, "");
      if (ori_c_and_v == null || STRING.isBlank(ori_c_and_v)) continue;
      String[] c_and_v = ori_c_and_v.split(",");
      if (c_and_v.length > 0) {
        if (c_and_v.length == 1) {
          //&uid=xxx;
          //eq
          conditions.add(Tuple.t3(f, Criterion.EQ, c_and_v));
        } else {
          conditions.add(Tuple.t3(f, Criterion.of(c_and_v[0]), Arrays.copyOfRange(c_and_v, 1, c_and_v.length)
          ));
        }
      }
    }
    return this.where(conditions);
  }



}
