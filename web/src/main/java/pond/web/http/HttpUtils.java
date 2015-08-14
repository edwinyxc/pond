package pond.web.http;

import pond.common.S;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by ed on 15-7-12.
 */
public class HttpUtils {

  public static <E> void appendToMap(Map<String, List<E>> map, String name, E value) {
    List<E> values;
    if ((values = map.get(name)) != null) values.add(value);
    else map.put(name, S._tap(new ArrayList<E>(), list -> list.add(value)));
  }

  public static <E> void appendToMap(Map<String, List<E>> map, String name, List<E> values) {
    for (E val : values) {
      appendToMap(map, name, val);
    }
  }

  public static <K, V> Map<K, V> lockMap(Map<K, V> map) {
    if (map == null) return Collections.emptyMap();
    return Collections.unmodifiableMap(map);
  }
}
