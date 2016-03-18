package pond.common;

import pond.common.spi.JsonService;

import java.util.List;
import java.util.Map;

/**
 * JSON api
 */
public class JSON {

  static JsonService json = SPILoader.service(JsonService.class);

  public static List parseArray(String s) {
    return json.fromString(List.class, s);
  }

  public static Map parse(String s) {
    return json.fromString(Map.class, s);
  }

  public static String stringify(Object m) {
    return json.toString(m);
  }

}
