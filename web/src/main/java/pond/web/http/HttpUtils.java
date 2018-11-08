//package pond.web.http;
//
//import pond.common.S;
//import pond.web.Request;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//
///**
// * Created by ed on 15-7-12.
// */
//public class HttpUtils {
//
//  /**
//   * <pre>
//   * Get remote ip using following strategy:
//   *
//   * header[x-forwarded-for]
//   * header[Proxy-Client-IP]
//   * header[WL-Proxy-Client-IP]
//   * req.remoteIp
//   * </pre>
//   *
//   * @param req
//   * @return
//   */
//  public static String getRealIp(Request req) {
//    CharSequence ip = req.header("x-forwarded-for");
//    if (ip == null) return null;
//    if (ip.length() == 0 || "unknown".equalsIgnoreCase(String.valueOf(ip))) {
//      ip = req.header("Proxy-Client-IP");
//    }
//    if (ip.length() == 0 || "unknown".equalsIgnoreCase(String.valueOf(ip))) {
//      ip = req.header("WL-Proxy-Client-IP");
//    }
//    if (ip.length() == 0 || "unknown".equalsIgnoreCase(String.valueOf(ip))) {
//      ip = req.remoteIp();
//    }
//    return String.valueOf(ip);
//  }
//
//  public static <E> void appendToMap(Map<String, List<E>> map, String name, E value) {
//    List<E> values;
//    if ((values = map.get(name)) != null) values.add(value);
//    else map.put(name, S._tap(new ArrayList<E>(), list -> list.add(value)));
//  }
//
//  public static <E> void appendToMap(Map<String, List<E>> map, String name, List<E> values) {
//    for (E val : values) {
//      appendToMap(map, name, val);
//    }
//  }
//
//  public static <K, V> Map<K, V> lockMap(Map<K, V> map) {
//    if (map == null) return Collections.emptyMap();
//    return Collections.unmodifiableMap(map);
//  }
//}
