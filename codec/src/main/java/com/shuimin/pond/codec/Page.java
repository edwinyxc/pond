package com.shuimin.pond.codec;

import com.shuimin.common.f.Tuple;
import com.shuimin.pond.core.Request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.shuimin.common.S._notNullElse;

/**
 * Created by ed on 14-5-20.
 */
public class Page extends HashMap<String, Object> {

    public static final String DATA = "rows";
    public static final String PG_IDX = "page";
    public static final String PG_LEN = "rows";
    public static final String PG_SIZE = "total";
    public static final String REC_SIZE = "records";

    public Page(Integer pgIdx, Integer pgLen) {
        this.put(PG_LEN, pgLen);
        this.put(PG_IDX, pgIdx);
        this.put(PG_SIZE, pgIdx == null ? 0 : pgIdx);
        this.put(REC_SIZE, pgLen == null ? 0 : pgLen);
    }


    public static Page of(Request r) {

        Integer pgIdx = _notNullElse(r.paramInt(PG_IDX), 1);
        Integer pgLen = _notNullElse(r.paramInt(PG_LEN), 0);
        Page page = new Page(pgIdx,pgLen);

        return page;
    }

    public static int getLimit(Request req) {
        return _notNullElse(req.paramInt(PG_LEN), 0);
    }

    public static int getOffset(Request req) {
        return getLimit(req) * (_notNullElse(req.paramInt(PG_IDX), 1) - 1);
    }

    public static boolean allowPage(Request req) {
        return req.param(PG_LEN) != null;
    }

    public Page fulfill(Tuple<List<Map<String, Object>>, Integer> x) {
        return fulfill(x._a, x._b);
    }

    public Page fulfill(List<Map<String, Object>> data, int records) {
        Integer pg_len = (Integer) this.get(PG_LEN);
        if (pg_len == null) {
            pg_len = data.size();
        }
        if (pg_len == 0) {
            //top max return
            pg_len = 9999;
        }
        put(PG_SIZE, Math.ceil((double) records / (double) pg_len));
        put(REC_SIZE, records);
        put(DATA, data);
        return this;
    }


    public Object data() {
        return this.get(DATA);
    }

    public Integer getOffset() {
        return ((Integer) this.get(PG_IDX) - 1) * getLimit();
    }

    public Integer getLimit() {
        return (Integer) this.get(PG_LEN);
    }
}
