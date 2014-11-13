package pond.codec;

import pond.common.S;
import pond.common.f.Tuple;
import pond.core.Ctx;
import pond.core.Pond;
import pond.core.Request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pond.common.sql.SqlSelect;
import pond.common.sql.Sql;
import pond.db.DB;
import pond.db.Record;

import static pond.common.S._for;
import static pond.common.S._getOrSet;
import static pond.common.S._notNullElse;

public class ReqQuery {

    public static final String SORD = "ReqQuery.sord";
    public static final String SORDF = "ReqQuery.sordf";

    public static final String DATA = "ReqQuery.data";
    public static final String PG_IDX = "ReqQuery.pg_idx";
    public static final String PG_LEN = "ReqQuery.pg_len";
    public static final String PG_SIZE = "ReqQuery.pg_size";
    public static final String REC_SIZE = "ReqQuery.rec_size";

    public static SqlSelect sqlFromReq(Request req, Record proto) {
        String tb_name = proto.table();
        Set<String> fields = proto.declaredFields();

        SqlSelect sql =
                Sql.select(fields.toArray(new String[fields.size()]))
                        .from(tb_name)
                        .where(req.toQuery(proto.declaredFields()));

        Ctx ctx = req.ctx();
        String N_SORD = _getOrSet(ctx.pond.config, SORD, "_sord");
        String N_SORDF = _getOrSet(ctx.pond.config, SORDF, "_sordf");
        // sort
        String sord = req.param(N_SORD);
        String sord_f = req.param(N_SORDF);

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


    public static Page queryForPage(Request req, Record p) {
        Pond pond = req.ctx().pond;
        DB db = (DB) (pond.attr(Pond.DEFAULT_DB));
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
        final String N_PG_IDX ;
        final String N_PG_LEN ;
        final String N_PG_SIZE ;
        final String N_REC_SIZE ;
        Map config;

        public Page( Map config ) {
            this.config = config;
            N_DATA = _getOrSet(config, DATA, "rows");
            N_PG_IDX = _getOrSet(config, PG_IDX, "page");
            N_PG_LEN = _getOrSet(config, PG_LEN, "rows");
            N_PG_SIZE = _getOrSet(config, PG_SIZE, "total");
            N_REC_SIZE = _getOrSet(config, REC_SIZE, "records");
        }

        public Page(Integer pgIdx, Integer pgLen, Map config) {
            this(config);
            this.take(pgIdx,pgLen);
        }

        public Page take(Integer pgIdx, Integer pgLen) {
            this.put(N_DATA, pgLen);
            this.put(N_PG_IDX, pgIdx);
            this.put(N_PG_SIZE, pgIdx == null ? 0 : pgIdx);
            this.put(N_REC_SIZE, pgLen == null ? 0 : pgLen);
            return this;
        }


        public static Page of(Request r) {
            Page page = new Page(r.ctx().pond.config);
            Integer pgIdx = _notNullElse(r.paramInt(page.N_PG_IDX), 1);
            Integer pgLen = _notNullElse(r.paramInt(page.N_PG_LEN), 0);
            return page.take(pgIdx, pgLen);
        }

        public int getLimit(Request req) {
            return _notNullElse(req.paramInt(N_PG_LEN), 0);
        }

        public int getOffset(Request req) {
            return getLimit(req) * (_notNullElse(req.paramInt(N_PG_IDX), 1) - 1);
        }

        public boolean allowPage(Request req) {
            return req.param(N_PG_LEN) != null;
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
