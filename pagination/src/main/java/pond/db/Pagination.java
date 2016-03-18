package pond.db;

import pond.common.S;
import pond.common.f.Function;
import pond.db.sql.Sql;
import pond.db.sql.SqlSelect;

import java.util.List;
import java.util.Map;
import java.util.Set;

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


  public static String[] sortOrdersFromReq(Map<String, Object> req, Iterable<String> declaredFields) {

    String N_SORDF = S.avoidNull(S.config.get(Pagination.class, SORDF), "_sordf");

    String sord_f = (String) req.get(N_SORDF);// &_sordf=a  &_sordf=a,b

    if (sord_f == null) return null;

    return sord_f.split(",");
  }

  public static Function<SqlSelect, SqlSelect> queryAppender(Map<String, Object> req, Record proto) {

    return select -> {
      Set<String> fields = proto.declaredFieldNames();
      select.where(Sql.queriesFromReq(req, fields));
      select.orderBy(sortOrdersFromReq(req, fields));
      return select;
    };

  }

  public static Function<Page, JDBCTmpl> queryForPage(Map<String, Object> req, SqlSelect select, Class<? extends Record> cls) {
    return queryForPage(req, select, Prototype.proto(cls));
  }

  public static Function<Page, JDBCTmpl> queryForPage(Map<String, Object> req, SqlSelect _select, Record proto) {
    return t -> {
      Page page = Page.of(req);
      SqlSelect select = Pagination.queryAppender(req, proto).apply(_select);

      if (page.allowPage(req))
        select.offset(page.getOffset(req)).limit(page.getLimit(req));

      List<Record> data = t.query(select);

      int count = t.count(select.count());

      List<Map<String, Object>> view = _for(data).map(Record::view).toList();

      return page.fulfill(view, count);
    };
  }

}
