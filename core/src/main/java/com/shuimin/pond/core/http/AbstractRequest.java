package com.shuimin.pond.core.http;

import com.shuimin.common.S;
import com.shuimin.pond.core.Request;
import com.shuimin.pond.core.exception.UnexpectedException;
import com.shuimin.pond.core.kernel.PKernel;
import com.shuimin.pond.core.spi.Logger;
import com.shuimin.pond.core.spi.MultipartRequestResolver;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static com.shuimin.common.S._notNullElse;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

public abstract class AbstractRequest implements Request {
    MultipartRequestResolver multipartRequestResolver
            = PKernel.getService(MultipartRequestResolver.class);
    Logger logger = Logger.createLogger(Request.class);
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
        return _notNullElse(headers().get(string),new String[0]);
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

    @Override
    public InputStream file(String name) {
        if(!multipartRequestResolver.isMultipart(this)){
            logger.warn("req not multipart!");
            return null;
        }
        try {
            return multipartRequestResolver.paramUploadFile(this,name);
        } catch (IOException e) {
            logger.warn(e.getMessage());
            throw new UnexpectedException(e);
        }
    }

}
