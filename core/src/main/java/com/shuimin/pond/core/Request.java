package com.shuimin.pond.core;

import com.shuimin.common.f.Tuple;
import com.shuimin.common.sql.Criterion;
import com.shuimin.pond.core.db.Record;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * event
 *
 * @author ed
 */
public interface Request {

    InputStream in() throws IOException;

    String path();

    String uri();

    @Deprecated
    Locale locale();

    Map<String, String[]> headers();

    String[] header(String string);

    Map<String, String[]> params();

    String param(String para);

    Integer paramInt(String para);

    Boolean paramBool(String para);

    Double paramDouble(String para);

    Request param(String para, String value);

    String[] params(String para);

    String method();

    String remoteIp();

    Iterable<Cookie> cookies();

    Cookie cookie(String s);

    //    HttpServletRequest raw();
    String characterEncoding();

    default public List<Tuple.T3<String, Criterion, Object[]>>
    getQuery(Record r, Request req) {
        List<Tuple.T3<String, Criterion, Object[]>>
                conditions = new ArrayList<>();
        for (String f : r.fields()) {
            String k = f;
            String[] c_and_v = req.params(f);
            if (c_and_v != null) {
                if (c_and_v.length == 1) {
                    //&uid=xxx;
                    //eq
                    conditions.add(Tuple.t3(f, Criterion.EQ, c_and_v));
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
}
