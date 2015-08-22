package pond.common;

import pond.common.spi.JsonService;

import java.util.Map;

/**
 * JSON api
 */
public class JSON {

  static JsonService json = SPILoader.service(JsonService.class);

  public static Map parse(String s) {
    return json.fromString(Map.class, s);
  }

  public static String stringify(Object m) {
    return json.toString(m);
  }

}
