package pond.web;


import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.multipart.FileUpload;
import pond.common.S;
import pond.common.STRING;
import pond.common.f.Function;
import pond.web.http.HttpCtx;

import java.io.InputStream;
import java.net.URI;
import java.util.*;

/**
 * event
 *
 * @author ed
 */
public interface Request {


//    class CanonicalParam<T> {
//        private final ParamType type;
//        private final ParamIn in;
//        private final T val;
//
//        public CanonicalParam(ParamType type, ParamIn in, T val) {
//            this.type = type;
//            this.in = in;
//            this.val = val;
//        }
//    }

    default String method() {
        return ctx().method();
    }

    default String remoteIp() {
        return ctx().remoteAddress().toString();
    }

    default InputStream in() {
        var body = (HttpCtx.Body)ctx()::bind;
        return body.bodyAsInputStream();
    }

    default String uri(){
        return ctx().uri();
    }

    default Map<String, List<String>> headers(){
        var rebind = (HttpCtx.Headers)ctx()::bind;
        return rebind.headers();
    }

    default Map<String, List<String>> queries() {
        var rebind = (HttpCtx.Queries)ctx()::bind;
        return rebind.queries();
    }

    default Map<String, List<String>> inUrlParams(){
        var queries = (HttpCtx.Queries)ctx()::bind;
        return queries.inUrlParams();
    }

    default Map<String, List<String>> formData() {
        var body = (HttpCtx.Body)ctx()::bind;
        if(body.bodyIsMultipart()){
            try {
                return body.bodyAsMultipart().attrs();
            } catch (IllegalAccessException ignore) { }
        }
        return body.bodyAsForm();
    }

    default Map<String, List<FileUpload>> files(){
        var body = (HttpCtx.Body)ctx()::bind;
        if(body.bodyIsMultipart()) {
            try {
                return body.bodyAsMultipart().files();
            } catch (IllegalAccessException ignore) { }
        }
        return Collections.emptyMap();
    }

//    default CanonicalParam<List<String>> canonicalParams(String name) {
//        List<String> f;
//        ParamIn in;
//        if ((f = queries().get(name)).size() > 0) {
//            in = ParamIn.QUERY;
//        } else if ((f = inUrlParams().get(name)).size() > 0) {
//            in = ParamIn.PATH;
//        } else if ((f = formData().get(name)).size() > 0) {
//            in = ParamIn.FORM_DATA;
//        } else {
//            return null;
//        }
//        return new CanonicalParam<>(ParamType.STRING, in, f);
//    }
//
//    default CanonicalParam<List<String>> canonicalHeaders(String name) {
//        List<String> ret = headers(name);
//        return ret.size() > 0
//                ? new CanonicalParam<>(ParamType.STRING, ParamIn.HEADER, ret)
//                : null;
//    }
//
//    default CanonicalParam<List<UploadFile>> canonicalFiles(String name) {
//        List<UploadFile> ret = files(name);
//        return  ret.size() > 0
//                ? new CanonicalParam<>( ParamType.FILE, ParamIn.FORM_DATA, ret)
//                : null;
//    }
//
//    default <T> CanonicalParam<T> canonicalBody(Function<T, InputStream> bodyParser) {
//        return new CanonicalParam<>(ParamType.SCHEMA, ParamIn.BODY, bodyParser.apply(in()));
//    }

    default Map<String, List<String>> params() {
        Map<String, List<String>> all_params = new HashMap<>();
        all_params.putAll(queries());
        all_params.putAll(formData());
        all_params.putAll(inUrlParams());
        return all_params;
    }



    default Map<String, Cookie> cookies(){
        var bind = (HttpCtx.Cookies) ctx()::bind;
        var set = bind.cookies();
        var ret = new HashMap<String, Cookie>();
        for(Cookie c : set) {
            ret.put(c.name(), c);
        }
        return ret;
    }

    default String path() {
        return S._try_ret(() -> new URI(this.uri()).getPath());
    }

    default Cookie cookie(String s) {
        return cookies().get(s);
    }

    default List<String> headers(String string) {
        var ctx = (HttpCtx)this.ctx()::bind;
        return headers().get(ctx.get(HttpCtx.CONFIG).isHeaderCaseSensitive() ? string : string.toLowerCase());
    }

    default String header(String string) {
        return S._for(headers(string)).first();
    }

    default List<String> params(String para) {
        return params().get(para);
    }

    default String param(String para) {
        String a = S._for(params(para)).first();
        if (a == null || "".equals(a) || "null".equals(a) || "undefined".equals(a)) {
            return null;
        }
        return a;
    }

    default String paramCheck(String key, Function<Boolean, String> checker, String err_msg) {
        String ret = param(key);
        if (checker.apply(ret)) {
            return ret;
        }
        throw new EndToEndException(400, err_msg);
    }

    /*
    default <R> R paramConvert(String key, Function<R, String> converter, String err_msg) {
        String ret = query(key);
        try {
            return converter.apply(ret);
        } catch (Exception e) {
            Pond.logger.warn("convert error:", e);
            throw new EndToEndException(400, e.getMessage() + err_msg);
        }
    }*/

    default String paramNonBlank(String key) {
        return paramCheck(key, STRING::notBlank, key + "can not be blank");
    }

    default String paramNonBlank(String key, String err_msg) {
        return paramCheck(key, STRING::notBlank, err_msg);
    }

    default String paramNonNull(String key, String err_msg) {
        return paramCheck(key, Objects::nonNull, err_msg);
    }

//    default void query(String key, String val) {
//        .appendToMap(queries(), key, val);
//    }

    //upload file
    default List<FileUpload> files(String file) {
        return files().get(file);
    }

    default FileUpload file(String file) {
        return S._for(files(file)).first();
    }


    HttpCtx ctx();

    /**
     * Returns headers queries as a Map
     */
    default Map<String, Object> toMap() {
        Map<String, Object> ret = new HashMap<>();
        //ret.putAll(S._for(attrs()).map(attr -> S._for(attr).limit()).val());
        ret.putAll(S._for(params()).map(param -> S._for(param).first()).val());
        return ret;
    }


}
