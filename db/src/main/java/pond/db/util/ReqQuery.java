package pond.db.util;

import pond.common.S;
import pond.common.f.Tuple;
import pond.db.DB;
import pond.db.Record;
import pond.db.sql.Criterion;
import pond.db.sql.Sql;
import pond.db.sql.SqlSelect;

import java.util.*;

import static pond.common.S._for;
import static pond.common.S._getOrSet;

public class ReqQuery {

  static Map CONFIG = new HashMap<>();

  public static final String SORD = "ReqQuery.sord";
  public static final String SORDF = "ReqQuery.sordf";

  public static final String DATA = "ReqQuery.data";
  public static final String PG_IDX = "ReqQuery.pg_idx";
  public static final String PG_LEN = "ReqQuery.pg_len";
  public static final String PG_SIZE = "ReqQuery.pg_size";
  public static final String REC_SIZE = "ReqQuery.rec_size";

  /**
   * Parse a query from Request
   *
   * @param declaredFields
   * @return
   */
  public static List<Tuple.T3<String, Criterion, Object[]>>
  reqToQuery(Map<String, Object> req, Iterable<String> declaredFields) {
    List<Tuple.T3<String, Criterion, Object[]>>
        conditions = new ArrayList<>();
    for (String f : declaredFields) {
      String ori_c_and_v = (String) req.getOrDefault(f, "");
      if (ori_c_and_v == null || S.str.isBlank(ori_c_and_v)) continue;
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

  public static SqlSelect sqlFromReq(Map<String, Object> req, Record proto) {
    String tb_name = proto.table();
    Set<String> fields = proto.declaredFieldNames();

    SqlSelect sql =
        Sql.select(fields.toArray(new String[fields.size()]))
            .from(tb_name)
            .where(reqToQuery(req, proto.declaredFieldNames()));

    //Ctx ctx = req.ctx();
    String N_SORD = _getOrSet(CONFIG, SORD, "_sord");
    String N_SORDF = _getOrSet(CONFIG, SORDF, "_sordf");

    // sort
    String sord = (String) req.get(N_SORD);
    String sord_f = (String) req.get(N_SORDF);

    if (S.str.notBlank(sord)
        && S.str.notBlank(sord_f)
        ) {
      String order;
      if (fields.contains(sord_f)) {
        order = sord_f;
      } else
        throw new RuntimeException("sordf not valid");
      if (sord.equalsIgnoreCase("desc")) {
        sql.orderBy(order + " desc");
      } else {
        sql.orderBy(order + " asc");
      }
    }
    return sql;
  }

  public static Page queryForPage(Map req, Record p, DB db) {
    return db.get(tmpl -> {
      Page page = Page.of(req);
      SqlSelect select = sqlFromReq(req, p);
      if (page.allowPage(req))
        select.offset(page.getOffset(req))
            .limit(page.getLimit(req));
      List<Record> data =
          tmpl.query(p.mapper(), select.tuple());
      int count = tmpl.count(select.count().tuple());
      List<Map<String, Object>> view =
          _for(data).map(Record::view).toList();
      return page.fulfill(view, count);
    });
  }

  @Deprecated
  public static Page queryForPage(DB db, Map req, Record p) {
    return db.get(tmpl -> {
      Page page = Page.of(req);
      SqlSelect select = sqlFromReq(req, p);
      if (page.allowPage(req))
        select.offset(page.getOffset(req))
            .limit(page.getLimit(req));
      List<Record> data =
          tmpl.query(p.mapper(), select.tuple());
      int count = tmpl.count(select.count().tuple());
      List<Map<String, Object>> view =
          _for(data).map(Record::view).toList();
      return page.fulfill(view, count);
    });
  }

  public static class Page extends HashMap<String, Object> {


    final String N_DATA;
    final String N_PG_IDX;
    final String N_PG_LEN;
    final String N_PG_SIZE;
    final String N_REC_SIZE;
    Map config;

    public Page(Map config) {
      this.config = config;
      N_DATA = _getOrSet(config, DATA, "rows");
      N_PG_IDX = _getOrSet(config, PG_IDX, "page");
      N_PG_LEN = _getOrSet(config, PG_LEN, "rows");
      N_PG_SIZE = _getOrSet(config, PG_SIZE, "total");
      N_REC_SIZE = _getOrSet(config, REC_SIZE, "records");
    }

    public Page(Integer pgIdx, Integer pgLen, Map config) {
      this(config);
      this.take(pgIdx, pgLen);
    }

    public Page take(Integer pgIdx, Integer pgLen) {
      this.put(N_DATA, pgLen);
      this.put(N_PG_IDX, pgIdx);
      this.put(N_PG_SIZE, S.avoidNull(pgIdx, 0));
      this.put(N_REC_SIZE, S.avoidNull(pgLen, 0));
      return this;
    }


    public static Page of(Map r) {
      Page page = new Page(CONFIG);
      Integer pgIdx = Integer.parseInt((String) r.getOrDefault(page.N_PG_IDX, "1"));
      Integer pgLen = Integer.parseInt((String) r.getOrDefault(page.N_PG_LEN, "0"));
      return page.take(pgIdx, pgLen);
    }

    public int getLimit(Map req) {
      return Integer.parseInt((String) req.getOrDefault(N_PG_LEN, "0"));
    }

    public int getOffset(Map req) {
      return getLimit(req) * (Integer.parseInt((String) req.getOrDefault(N_PG_IDX, "1")) - 1);
    }

    public boolean allowPage(Map req) {
      return req.get(N_PG_LEN) != null;
    }

    public Page fulfill(Tuple<List<Map<String, Object>>, Integer> x) {
      return fulfill(x._a, x._b);
    }

    public Page fulfill(List<Map<String, Object>> data, int records) {
      Integer pg_len = (Integer) this.get(N_PG_LEN);
      if (pg_len == null) {
        pg_len = data.size();
      }
      if (pg_len == 0) {
        //top max return
        pg_len = 9999;
      }
      put(N_PG_SIZE, Math.ceil((double) records / (double) pg_len));
      put(N_REC_SIZE, records);
      put(N_DATA, data);
      return this;
    }


    public Object data() {
      return this.get(N_DATA);
    }

    public Integer getOffset() {
      return ((Integer) this.get(N_PG_IDX) - 1) * getLimit();
    }

    public Integer getLimit() {
      return (Integer) this.get(N_PG_LEN);
    }
  }
}
