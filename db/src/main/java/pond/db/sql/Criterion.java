package pond.db.sql;

import pond.common.f.Function;
import pond.db.sql.dialect.Dialect;

import static java.lang.String.format;
import static pond.common.S._for;

/**
 * Created by ed on 14-5-22.
 */
public enum Criterion {

  EQ("eq", (k) -> o -> "" + k + " = ? "),
  LIKE("lk", (k) -> o -> "" + k + " LIKE ?"),
  //    STARTS_WITH("sw", (k) -> o -> ""+k+" LIKE ?"),
//    ENDS_WITH("ew", (k) -> o -> ""+k+" LIKE ?"),
  NOT_LIKE("nlk", (k) -> o -> "" + k + " NOT LIKE ?"),
  GREATER_THAN("gt", (k) -> o -> "" + k + " > ?"),
  GREATER_THAN_E("gte", (k) -> o -> "" + k + " >= ?"),
  LITTLE_THAN("lt", (k) -> o -> "" + k + " < ?"),
  LITTLE_THAN_E("lte", (k) -> o -> "" + k + " <= ?"),
  NOT_EQ("neq", (k) -> o -> format("%s <> ?", k)),
  BETWEEN("btwn", (k) -> o -> format("(%s BETWEEN ? and ?)", k)),
  IN("in", (k) -> o -> k + " IN (" + String.join(",", _for((String[]) o).map(i -> "?").join()) + ")"),
  NOT_IN("nin", (k) -> o -> k + " NOT IN (" + String.join(",", _for((String[]) o).map(i -> "?").join()) + ")");
  /**
   * URL:
   */


  private String url;

  /**
   * URL :&[key]=like,[value]
   * <p>
   * SQL : [key] LIKE %[value]%
   */
  private Function<Function<String, Object[]>, String> sql;

  Criterion(String url, Function<Function<String, Object[]>, String> sql) {
    this.sql = sql;
    this.url = url;
  }


  public static Criterion of(String str) {

    String s = str.toLowerCase();
    switch (s) {
      case "eq":
        return EQ;
      case "lk":
        return LIKE;
//            case "sw":
//                return STARTS_WITH;
//            case "ew":
//                return ENDS_WITH;
      case "nlk":
        return NOT_LIKE;
      case "gt":
        return GREATER_THAN;
      case "gte":
        return GREATER_THAN_E;
      case "lt":
        return LITTLE_THAN;
      case "lte":
        return LITTLE_THAN_E;
      case "neq":
        return NOT_EQ;
      case "btwn":
        return BETWEEN;
      case "in":
        return IN;
      case "nin":
        return NOT_IN;
      default:
        return EQ;
    }
  }

  public String URL() {
    return url;
  }

  public String prepare(String k, Object[] v, Dialect d) {
    if (d != null) {
      return sql.apply(d.wrapKey(k)).apply(v);
    }
    return sql.apply(k).apply(v);
  }

}
