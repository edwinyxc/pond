package pond.db;

import pond.common.S;
import pond.common.STRING;
import pond.common.f.Function;
import pond.common.f.Tuple;
import pond.db.sql.Criterion;
import pond.db.sql.SqlSelect;

import java.util.*;

import static pond.common.S._for;


public class Pagination {

//  static Map CONFIG = new HashMap<>();

  //  public static final String SORD = "Pagination.sord";
  public static final String SORDF = "sordf";

  public static final String DATA = "data";
  public static final String PG_IDX = "pg_idx";
  public static final String PG_LEN = "pg_len";
  public static final String PG_SIZE = "pg_size";
  public static final String REC_SIZE = "rec_size";


  /**
   * Parse a query from Request
   *
   * @param declaredFields
   * @return
   */
  public static List<Tuple.T3<String, Criterion, Object[]>>
  reqToQuery(Map<String, Object> req, Iterable<String> declaredFields) {
    List<Tuple.T3<String, Criterion, Object[]>> conditions = new ArrayList<>();
    for (String f : declaredFields) {
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
    return conditions;
  }

  public static String[] reqToOrders(Map<String, Object> req, Iterable<String> declaredFields) {

    String N_SORDF = S.avoidNull(S.config.get(Pagination.class, SORDF), "_sordf");

    String sord_f = (String) req.get(N_SORDF);// &_sordf=a  &_sordf=a,b

    if (sord_f == null) return null;

    return sord_f.split(",");
  }

  public static Function<SqlSelect, SqlSelect> sqlFromReq(Map<String, Object> req, Record proto) {

    return select -> {
      Set<String> fields = proto.declaredFieldNames();
      select.where(reqToQuery(req, fields));
      select.orderBy(reqToOrders(req, fields));
      return select;
    };

  }

  public static Function<Page, JDBCTmpl> queryForPage(Map<String, Object> req, SqlSelect _select, Record proto) {
    return t -> {
      Page page = Page.of(req);
      SqlSelect select = Pagination.sqlFromReq(req, proto).apply(_select);

      if (page.allowPage(req))
        select.offset(page.getOffset(req)).limit(page.getLimit(req));

      List<Record> data = t.query(select);

      int count = t.count(select.count());

      List<Map<String, Object>> view = _for(data).map(Record::view).toList();

      return page.fulfill(view, count);

    };
  }

}
