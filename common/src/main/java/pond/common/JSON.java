package pond.common;

import pond.common.spi.JsonService;
import pond.common.spi.json.AlibabaJson;

import java.util.Map;

/**
 * JSON api
 */
public class JSON {
    static JsonService json = new AlibabaJson();

    public static Map parse(String s){
        return json.fromString(Map.class, s);
    }

    public static String stringify(Object m) {
        return json.toString(m);
    }

}
