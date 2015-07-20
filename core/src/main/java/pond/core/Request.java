package pond.core;


import pond.common.S;
import pond.core.http.Cookie;
import pond.core.http.HttpUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * event
 *
 * @author ed
 */
public interface Request {

    String method();

    String remoteIp();

    String characterEncoding();

    //InputStream in() throws IOException;

    String uri();

    Map<String, List<String>> headers();

    Map<String, List<String>> params();

    Map<String, List<String>> attrs();

    Map<String, List<UploadFile>> files();

    Map<String, Cookie> cookies();

    default String path() {
        return S._try_ret(() -> new URI(uri()).getPath());
    }

    default Cookie cookie(String s) {
        return cookies().get(s);
    }

    default List<String> headers(String string) {
        return headers().get(string);
    }

    default String header(String string) {
        return S._for(headers(string)).first();
    }

    default List<String> params(String para) {
        return params().get(para);
    }

    default String param(String para) {
        return S._for(params(para)).first();
    }

    default void param(String key, String val) {
        HttpUtils.appendToMap(params(), key, val);
    }

    default List<String> attrs(String attr) {
        return attrs().get(attr);
    }

    default String attr(String attr) {
        return S._for(attrs(attr)).first();
    }

    //upload File
    default List<UploadFile> files(String file) {
        return files().get(file);
    }

    default UploadFile file(String file) {
        return S._for(files(file)).first();
    }

    default Integer paramInt(String para) {
        return S._try_ret(() -> Integer.parseInt(param(para)));
    }

    default Boolean paramBool(String para) {
        return S._try_ret(() -> Boolean.parseBoolean(param(para)));
    }

    default Double paramDouble(String para) {
        return S._try_ret(() -> Double.parseDouble(param(para)));
    }

    default Long paramLong(String para) {
        return S._try_ret(() -> Long.parseLong(param(para)));
    }

    default Integer attrInt(String para) {
        return S._try_ret(() -> Integer.parseInt(attr(para)));
    }

    default Boolean attrBool(String para) {
        return S._try_ret(() -> Boolean.parseBoolean(attr(para)));
    }

    default Double attrDouble(String para) {
        return S._try_ret(() -> Double.parseDouble(attr(para)));
    }

    default Long attrLong(String para) {
        return S._try_ret(() -> Long.parseLong(attr(para)));
    }

    default String argument(String arg) {
        String ret;
        ret = param(arg);
        if (ret == null)
            ret = attr(arg);
        return ret;
    }

    default Ctx ctx() {
        return CtxExec.get();
    }

    default Map<String, Object> toMap() {
        Map<String, Object> ret = new HashMap<>();
        ret.putAll(S._for(attrs()).map(attr -> S._for(attr).first()).val());
        ret.putAll(S._for(params()).map(param -> S._for(param).first()).val());
        return ret;
    }

    interface UploadFile {
        /**
         * attr name
         */
        String name();

        /**
         * original filename provided by client
         */
        String filename();

        /**
         * input for file
         */
        InputStream inputStream() throws IOException;

        /**
         * File
         */
        File file() throws IOException;
    }


}
