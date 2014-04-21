package com.shuimin.jtiny.core.http;

import com.shuimin.base.S;

import javax.servlet.http.Cookie;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class AbstractRequest implements Request {

    @Override
    public String path() {
        S._assert(uri(), "empty uri ");
        try {
            return new URL(uri()).getPath();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            S._lazyThrow(e);
        }
        return null;
    }

    @Override
    public String toString() {
        return "req[#uri = " + uri() + ","
            + "#headers" + S.dump(headers()) + ","
            + "#params" + S.dump(params())
            + "]";
    }

    @Override
    public String[] header(String string) {
        return headers().get(string);
    }

    @Override
    public String param(String para) {
        String[] ret = params().get(para);
        return ret != null && ret.length > 0 ? ret[0] : null;
    }

    @Override
    public Request param(String para, String value) {
        params().put(para, new String[]{value});
        return this;
    }

    @Override
    public String[] params(String name) {
        return params().get(name);
    }

    @Override
    public Cookie cookie(final String cookie_name) {
        return S._for(cookies()).grep((cookie) -> (cookie_name.equals(
            cookie.getName())))
            .first();
    }

}
