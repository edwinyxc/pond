package pond.web.http;

import pond.common.S;

import java.util.ArrayList;
import java.util.List;

public enum HttpMethod {

  GET(1), POST(2), PUT(4), DELETE(8), HEAD(16), OPTIONS(32), TRACE(128), CONNECT(256);

  int value = 0;

  HttpMethod(int i) {
    value = i;
  }

  public static List<HttpMethod> unMask(int i) {
    List<HttpMethod> ret = new ArrayList<>();
    for (HttpMethod m : HttpMethod.values()) {
      if (m.match(i)) {
        ret.add(m);
      }
    }
    return ret;
  }

  public static int mask(HttpMethod... x) {
    int mask = 0;
    for (HttpMethod m : x) {
      mask |= m.value;
    }
    return mask;
  }

  public static int maskAll() {
    return mask(GET, POST, PUT, DELETE, HEAD, OPTIONS, TRACE, CONNECT);
  }

  public static HttpMethod of(String _method) {
    String method = _method.trim();
    S._assert(method, "method null");
    if (method.equalsIgnoreCase("get")) {
      return GET;
    }
    if (method.equalsIgnoreCase("post")) {
      return POST;
    }
    if (method.equalsIgnoreCase("put")) {
      return PUT;
    }
    if (method.equalsIgnoreCase("delete")) {
      return DELETE;
    }
    if (method.equalsIgnoreCase("head")) {
      return HEAD;
    }
    if (method.equalsIgnoreCase("trace")) {
      return TRACE;
    }
    if (method.equalsIgnoreCase("options")) {
      return OPTIONS;
    }
    if (method.equalsIgnoreCase("connect")) {
      return CONNECT;
    }
    throw new IllegalArgumentException("method string not recognized");
  }

  public int value() {
    return this.value;
  }

  public boolean match(int test) {
    return (test & this.value) == this.value;
  }
}
