package pond.core;

import pond.common.S;

import javax.servlet.http.Cookie;
import java.net.MalformedURLException;
import java.net.URL;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static pond.common.S.avoidNull;

public abstract class AbstractRequest implements Request {
    @Override
    public Integer paramInt(String para) {
        String s = param(para);
        return s == null ? null : parseInt(s);
    }

    @Override
    public Boolean paramBool(String para) {
        String s = param(para);
        return s == null ? null : parseBoolean(s);
    }

    @Override
    public Double paramDouble(String para) {
        String s = param(para);
        return s == null ? null : parseDouble(s);
    }

    @Override
    public Long paramLong(String para) {
        String s = param(para);
        return s == null ? null : Long.parseLong(s);
    }

    @Override
    public String path() {
        S._assert(uri(), "empty uri ");
        try {
            return new URL(uri()).getPath();
        } catch (MalformedURLException e) {
            throw  new RuntimeException(e);
        }
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
        //TODO normalize input and output string
        return avoidNull(headers().get(string), new String[0]);
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
        return S._for(cookies()).filter((cookie) -> (cookie_name.equals(
                cookie.getName())))
                .first();
    }

}
