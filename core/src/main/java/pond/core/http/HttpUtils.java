package pond.core.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by ed on 15-7-12.
 */
public class HttpUtils {

    public static <E> void appendToMap(Map<String, List<E>> map, String name, E value) {
        List<E> values;
        if ((values = map.get(name)) != null) values.add(value);
        map.put(name, new ArrayList<E>());
    }
}
