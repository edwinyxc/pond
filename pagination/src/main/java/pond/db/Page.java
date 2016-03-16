package pond.db;

import pond.common.S;
import pond.common.f.Tuple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ed on 3/16/16.
 */

public class Page extends HashMap<String, Object> {

  //LABELS
  final String LB_DATA = S.avoidNull(S.config.get(Pagination.class, Pagination.DATA), "rows");
  final String LB_PG_IDX = S.avoidNull(S.config.get(Pagination.class, Pagination.PG_IDX), "page");
  final String LB_PG_LEN = S.avoidNull(S.config.get(Pagination.class, Pagination.PG_LEN), "rows");
  final String LB_PG_SIZE = S.avoidNull(S.config.get(Pagination.class, Pagination.PG_SIZE), "total");
  final String LB_REC_SIZE = S.avoidNull(S.config.get(Pagination.class, Pagination.REC_SIZE), "records");


  public Page(){}

  public Page(Integer pgIdx, Integer pgLen, Map config) {
    this.take(pgIdx, pgLen);
  }

  public Page take(Integer pgIdx, Integer pgLen) {
    this.put(LB_DATA, pgLen);
    this.put(LB_PG_IDX, pgIdx);
    this.put(LB_PG_SIZE, S.avoidNull(pgIdx, 0));
    this.put(LB_REC_SIZE, S.avoidNull(pgLen, 0));
    return this;
  }

  public static Page of(Map r) {
    Page page = new Page();
    Integer pgIdx = Integer.parseInt((String) r.getOrDefault(page.LB_PG_IDX, "1"));
    Integer pgLen = Integer.parseInt((String) r.getOrDefault(page.LB_PG_LEN, "0"));
    return page.take(pgIdx, pgLen);
  }

  public int getLimit(Map req) {
    return Integer.parseInt((String) req.getOrDefault(LB_PG_LEN, "0"));
  }

  public Integer getLimit() {
    return (Integer) this.get(LB_PG_LEN);
  }

  public Integer getOffset() {
    return ((Integer) this.get(LB_PG_IDX) - 1) * getLimit();
  }

  public int getOffset(Map req) {
    return getLimit(req) * (Integer.parseInt((String) req.getOrDefault(LB_PG_IDX, "1")) - 1);
  }

  public boolean allowPage(Map req) {
    return req.get(LB_PG_LEN) != null;
  }

  public Page fulfill(Tuple<List<Map<String, Object>>, Integer> x) {
    return fulfill(x._a, x._b);
  }

  public Page fulfill(List<Map<String, Object>> data, int records) {
    Integer pg_len = (Integer) this.get(LB_PG_LEN);
    if (pg_len == null) {
      pg_len = data.size();
    }
    if (pg_len == 0) {
      //top max return
      pg_len = Integer.MAX_VALUE;
    }
    put(LB_PG_SIZE, Math.ceil((double) records / (double) pg_len));
    put(LB_REC_SIZE, records);
    put(LB_DATA, data);
    return this;
  }

  public Object data() {
    return this.get(LB_DATA);
  }

}

