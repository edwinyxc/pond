package pond.core;


import pond.common.SPILoader;
import pond.common.f.Tuple;
import pond.common.sql.Criterion;
import pond.core.http.UploadFile;
import pond.core.spi.MultipartRequestResolver;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static pond.common.S._for;

/**
 * event
 *
 * @author ed
 */
public interface Request {

    final static MultipartRequestResolver mResolver =
            SPILoader.service(MultipartRequestResolver.class);

    InputStream in() throws IOException;

    String path();

    String uri();

    @Deprecated
    Locale locale();

    Map<String, String[]> headers();

    String[] header(String string);

    Map<String, String[]> params();

    String param(String para);

    default UploadFile paramFile(String name) {
        Object ret = this.toMap().get(name);
        if ( ret instanceof UploadFile ) {
            return (UploadFile) ret;
        }
        throw new RuntimeException("" + name + " is not a file");
    }

    Integer paramInt(String para);

    Boolean paramBool(String para);

    Double paramDouble(String para);

    Long paramLong(String para);

    Request param(String para, String value);

    String[] params(String para);

    String method();

    String remoteIp();

    Iterable<Cookie> cookies();

    Cookie cookie(String s);

    //    HttpServletRequest raw();
    String characterEncoding();

    default Ctx ctx() {
        throw new UnsupportedOperationException("use wrapper");
    }
//    Route route();
//
//    Route route(Route r);
//

    //TODO
    default List<Tuple.T3<String, Criterion, Object[]>>

    toQuery(Iterable<String> declaredFields) {
        List<Tuple.T3<String, Criterion, Object[]>>
                conditions = new ArrayList<>();
        for (String f : declaredFields) {
            String ori_c_and_v = this.param(f);
            if (ori_c_and_v == null) continue;
            String[] c_and_v = ori_c_and_v.split(",");
            if (c_and_v.length > 0) {
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


    default Map<String, Object> toMap() {
        if (mResolver.isMultipart(this)) {
            return mResolver.resolve(this);
        }
        Map<String, Object> map = new HashMap<>();

        _for(params()).each(e -> {
            Object val = e.getValue() != null && e.getValue().length > 0
                    ? e.getValue()[0]
                    : null;
            map.put(e.getKey(), val);
        });
        return map;
    }

}
