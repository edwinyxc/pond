package pond.web;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Session SPI, designed to provide a simple facade to read/write session.
 * The session store mechanism & policy depends on the implementation handle SPI provider
 */

public interface SessionStore {

  /**
   * Logger ID
   */
  Logger logger = LoggerFactory.getLogger(SessionStore.class);

  /**
   * Configs
   */

  /**
   * Session max age, seconds
   */
  static final String SESSION_MAX_AGE = "session_max_age";


  /**
   * Get sessionMap by sessionID
   *
   * @param <K>
   * @param <V>
   * @return map
   */
  <K, V> Map<K, V> get(String sessionID);

  /**
   * Creates a new entry with input Map;
   *
   * @return sessionID
   */
  String create(Map map);

  /**
   * Remove an entry
   */
  void remove(String sessionID);

  /**
   * update map to the store (replace or merge depends on the implementation
   */
  void update(String sessionID, Map map);

  /**
   * This method returns a copy handle the session map
   */
  Map<String, Map> all();

}
