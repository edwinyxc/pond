package com.shuimin.pond.core.db;

import com.shuimin.common.f.Tuple;
import com.shuimin.pond.core.Request;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.shuimin.common.S._notNullElse;
import static com.shuimin.pond.core.Pond.debug;

/**
 * Created by ed on 14-5-20.
 */
public class Page<E> extends HashMap<String, Object> {

    public static final String DATA = "rows";
    public static final String PG_IDX = "page";
    public static final String PG_LEN = "rows";
    public static final String PG_SIZE = "total";
    public static final String REC_SIZE = "records";

    private Page() {
        this.put(DATA, Collections.emptyList());
        this.put(PG_IDX, 1);
        this.put(PG_SIZE, 0);
        this.put(REC_SIZE, 0);
    }

    public static <E> Page<E> of(Request r) {
        Page<E> page = new Page<>();

        Integer pgIdx = _notNullElse(r.paramInt(PG_IDX), 1);
        debug(pgIdx);
        Integer pgLen = _notNullElse(r.paramInt(PG_LEN), 0);
        debug(pgLen);
        page.put(PG_IDX, pgIdx);
        page.put(PG_LEN, pgLen);

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

    public Page<E> fulfill(Tuple<List<E>, Integer> x) {
        return fulfill(x._a, x._b);
    }

    public Page<E> fulfill(List<E> data, int records) {
        put(DATA, data);
        put(PG_SIZE, Math.ceil(records / data.size()));
        put(REC_SIZE, records);
        return this;
    }

    @SuppressWarnings("uncheked")
    public List<E> data() {
        return (List<E>) this.get(DATA);
    }

    public Integer getOffset() {
        return ((Integer) this.get(PG_IDX) - 1) * getLimit();
    }

    public Integer getLimit() {
        return (Integer) this.get(PG_LEN);
    }
}
