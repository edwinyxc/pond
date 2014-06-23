package com.shuimin.common.sql;

import com.shuimin.common.f.Function;
import com.shuimin.common.f.Tuple;
import com.shuimin.pond.core.db.Record;
import com.shuimin.pond.core.Request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;

/**
 * Created by ed on 14-5-22.
 */
public enum Criterion {

    EQ("eq", (k) -> o -> format("%s = ? ", k)),
    LIKE("lk", (k) -> o -> format("%s LIKE %?%", k)),
    STARTS_WITH("sw", (k) -> o -> format("%s LIKE ?%", k)),
    ENDS_WITH("ew", (k) -> o -> format("%s LIKE %?", k)),
    NOT_LIKE("nlk", (k) -> o -> format("%s NOT LIKE %?%", k)),
    GREATER_THAN("gt", (k) -> o -> format("%s > ?", k)),
    GREATER_THAN_E("gte", (k) -> o -> format("%s >= ?", k)),
    LITTLE_THAN("lt", (k) -> o -> format("%s < ?", k)),
    LITTLE_THAN_E("lte", (k) -> o -> format("%s <= ?", k)),
    NOT_EQ("neq", (k) -> o -> format("%s <> ?", k)),
    BETWEEN("btwn", (k) -> o -> format("%s BETWEEN ? and ?", k)),
    IN("in", (k) -> o -> k + " IN (" + String.join(",", (String[]) o) + ")"),
    NOT_IN("nin", (k) -> o -> k + " NOT IN (" + String.join(",", (String[]) o) + ")");
    /**
     * URL:
     */


    private String url;

    /**
     *  URL :&[key]=like,[value]
     *
     *  SQL : [key] LIKE %[value]%
     */
    private Function<Function<String, Object[]>, String> sql;
    Criterion(String url, Function<Function<String, Object[]>, String> sql) {
        this.sql = sql;
        this.url = url;
    }

    public static List<Tuple.T3<String, Criterion, Object[]>>
    parseFromRequest(Record r, Request req) {
        List<Tuple.T3<String, Criterion, Object[]>>
                conditions = new ArrayList<>();
        for (String f : r.fields()) {
            String k = f;
            String[] c_and_v = req.params(f);
            if (c_and_v != null) {
                if (c_and_v.length == 1) {
                    //&uid=xxx;
                    //eq
                    conditions.add(Tuple.t3(f, EQ, c_and_v));
                } else {
                    conditions.add(Tuple.t3(f,
                            Criterion.of(c_and_v[0]),
                            Arrays.copyOfRange(c_and_v, 1, c_and_v.length)
                    ));
                }
            }
        }
        return conditions;
    }

    public static Criterion of(String str) {

        String s = str.toLowerCase();
        switch (s) {
            case "eq":
                return EQ;
            case "lk":
                return LIKE;
            case "sw":
                return STARTS_WITH;
            case "ew":
                return ENDS_WITH;
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

    public String prepare(String k, Object[] v) {
        return sql.apply(k).apply(v);
    }

}
