package pond.common.config;

import pond.common.S;

import java.util.HashMap;
import java.util.Map;

/**
 * A config impl using the System.properties
 */

public class SystemBasedConfig implements Config {

  SystemBasedConfig(){}

  @Override
  public String get(String name) {
    return System.getProperty(name);
  }

  @Override
  public Config set(String name, String val) {
    System.setProperty(name, val);
    return this;
  }

  @Override
  public Map<String, String> all() {
    Map<Object, Object> props = System.getProperties();
    Map<String, String> ret = new HashMap<>();
    for (Map.Entry<Object, Object> entry : props.entrySet()) {
      ret.put(entry.getKey().toString(),
              String.valueOf(entry.getValue()));
    }
    return ret;
  }


}
