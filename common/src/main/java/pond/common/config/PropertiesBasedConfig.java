package pond.common.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertiesBasedConfig implements Config {

  final Properties p;

  public PropertiesBasedConfig(Properties props) {
    p = props;
  }

  public Properties properties() {
    return p;
  }

  @Override
  public String get(String name) {
    return p.getProperty(name);
  }

  @Override
  public Config set(String name, String val) {
    p.setProperty(name, val);
    return this;
  }

  @Override
  public Map<String, String> all() {
    Map<String, String> ret = new HashMap<>();
    for (Map.Entry<Object, Object> entry : p.entrySet()) {
      ret.put(entry.getKey().toString(),
              String.valueOf(entry.getValue()));
    }
    return ret;
  }


}
