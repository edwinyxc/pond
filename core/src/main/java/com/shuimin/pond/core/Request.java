package com.shuimin.pond.core;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

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
}
