package com.shuimin.pond.core.server.jetty;

import com.shuimin.common.S;
import com.shuimin.pond.core.http.AbstractRequest;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author ed
 */
public class HSRequestWrapper extends AbstractRequest {

    HttpServletRequest _req;

    Map<String,String[]> paramsMap;

    public HSRequestWrapper(HttpServletRequest req) {
        _req = req;
    }

    @Override
    public InputStream in() throws IOException {
        return _req.getInputStream();
    }

    @Override
    public String uri() {
        return _req.getRequestURL().toString();
    }

    @Override
    public Locale locale() {
        return _req.getLocale();
    }

    @Override
    public Map<String, String[]> headers() {
        Map<String, String[]> ret = new HashMap<>();
        S._for(_req.getHeaderNames()).each((name) -> {
            ret.put(name, S.array.of(_req.getHeaders(name)));
        });
        return ret;
    }

    @Override
    public Map<String, String[]> params() {
        if(paramsMap == null) {
            paramsMap = new HashMap<>(_req.getParameterMap());
        }
        return paramsMap;
    }

    @Override
    public String method() {
        return _req.getMethod();
    }

    @Override
    public String remoteIp() {
        String ip = _req.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = _req.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = _req.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = _req.getRemoteAddr();
        }
        return ip;
    }

    @Override
    public Iterable<Cookie> cookies() {
        return S._for(_req.getCookies()).val();
    }

//    @Override
//    public HttpServletRequest raw() {
//        return _req;
//    }

    @Override
    public String characterEncoding() {
        return _req.getCharacterEncoding();
    }
}
