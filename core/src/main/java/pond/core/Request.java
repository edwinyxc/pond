package pond.core;


import pond.common.f.Tuple;
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
        return CtxExec.get();
    }
//    Route route();
//
//    Route route(Route r);
//
    //TODO



    default Map<String, Object> toMap() {
        /*multi-part request*/
        MultipartRequestResolver mResolver  = ctx().pond.multipart();
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
