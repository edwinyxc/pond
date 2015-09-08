package pond.web.spi;

import java.util.Map;

/**
 * Created by ed on 9/7/15.
 */
public class DefaultSession implements SessionStore {
  @Override
  public <K, V> Map<K, V> get(String sessionID) {
    return null;
  }

  @Override
  public String create(Map map) {
    return null;
  }

  @Override
  public void remove(String sessionID) {

  }

  @Override
  public void update(String sessionID, Map map) {

  }

  @Override
  public Map<String, Map> all() {
    return null;
  }
}
