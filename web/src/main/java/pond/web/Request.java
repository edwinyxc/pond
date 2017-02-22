package pond.web;


import pond.common.S;
import pond.common.STRING;
import pond.common.f.Function;
import pond.web.http.Cookie;
import pond.web.http.HttpUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * event
 *
 * @author ed
 */
public interface Request {

  String method();

  String remoteIp();

  InputStream in();

  String uri();

  Map<String, List<String>> headers();

  Map<String, List<String>> params();

  Map<String, List<UploadFile>> files();

  Map<String, Cookie> cookies();

  String path();

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

  default <R> R paramConvert(String key, Function<R, String> converter, String err_msg) {
    String ret = param(key);
    try {
      return converter.apply(ret);
    } catch (Exception e) {
      Pond.logger.warn("convert error:", e);
      throw new EndToEndException(400, e.getMessage() + err_msg);
    }
  }

  default String paramNonBlank(String key) {
    return paramCheck(key, STRING::notBlank, key + "can not be blank");
  }

  default String paramNonBlank(String key, String err_msg) {
    return paramCheck(key, STRING::notBlank, err_msg);
  }

  default String paramNonNull(String key, String err_msg) {
    return paramCheck(key, Objects::nonNull, err_msg);
  }

  default void param(String key, String val) {
    HttpUtils.appendToMap(params(), key, val);
  }

  //upload File
  default List<UploadFile> files(String file) {
    return files().get(file);
  }

  default UploadFile file(String file) {
    return S._for(files(file)).first();
  }

  @Deprecated
  default Integer paramInt(String para) {
    String data = param(para);
    if (STRING.isBlank(para)) return null;
    return S._try_ret(() -> Integer.parseInt(data));
  }

  @Deprecated
  default Boolean paramBool(String para) {
    String data = param(para);
    if (STRING.isBlank(data)) return null;
    return S._try_ret(() -> Boolean.parseBoolean(data));
  }

  @Deprecated
  default Double paramDouble(String para) {
    String data = param(para);
    if (STRING.isBlank(data)) return null;
    return S._try_ret(() -> Double.parseDouble(data));
  }

  @Deprecated
  default Long paramLong(String para) {
    String data = param(para);
    if (STRING.isBlank(para)) return null;
    return S._try_ret(() -> Long.parseLong(data));
  }

  HttpCtx ctx();

  /**
   * Returns all params as a Map
   */
  default Map<String, Object> toMap() {
    Map<String, Object> ret = new HashMap<>();
    //ret.putAll(S._for(attrs()).map(attr -> S._for(attr).limit()).val());
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
