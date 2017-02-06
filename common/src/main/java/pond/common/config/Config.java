package pond.common.config;


import pond.common.S;

import java.util.Map;

public interface Config {

  /**
   * Get a setting with specified name
   */
  String get(String name);

  /**
   * Get a setting under specified class.
   *
   * @param c    -- specified class
   * @param name -- entry's name
   */
  default String get(Class<?> c, String name) {
    return get(concatNames(c, name));
  }

  /**
   * Set an entry
   */
  Config set(String name, String val);

  /**
   * Set an entry of setting
   *
   * @param c    -- specified class
   * @param name -- entry's name
   * @param val  -- entry's value
   * @return self for fluid-style
   */
  default Config set(Class<?> c, String name, String val) {
    return set(concatNames(c, name), val);
  }

  /**
   * Returns all the settings
   */
  Map<String, String> all();

  default Map<String, String> all(String prefix) {
    return S._for(all()).filterByKey(name -> name.startsWith(prefix)).val();
  }

  default Map<String, String> all(Class<?> c) {
    return S._for(all()).filterByKey(name -> name.startsWith(c.getCanonicalName())).val();
  }

  final static Config system = new SystemBasedConfig();

  static String concatNames(Class<?> c, String name) {
    S._assertNotNull(c, name);
    return c.getCanonicalName() + "." + name;
  }
}
