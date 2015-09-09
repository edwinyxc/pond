package pond.web.spi;

import pond.common.Convert;
import pond.common.S;
import pond.common.struc.Cache;
import pond.common.struc.cache.OnTimeExpiredCache;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ed on 9/7/15.
 */
public class DefaultSessionStore implements SessionStore {

  final Cache<String, Map> mapCache;

  public DefaultSessionStore() {
    mapCache = new OnTimeExpiredCache<>(
        //default to half-hour
        1000 * Convert.toLong(S.avoidNull(S.config.get(SessionStore.class, SessionStore.SESSION_MAX_AGE), "1800")),
        //interval, default to 60s
        60 * 1000
    );
  }

  @Override
  @SuppressWarnings("unchecked")
  public <K, V> Map<K, V> get(String sessionID) {
    Map tmp = mapCache.get(sessionID);
    if (tmp == null) return null;
    return new HashMap<>(tmp);
  }

  @Override
  public String create(Map map) {
    String newSessionId = S.uuid.vid();
    mapCache.put(newSessionId, map);
    logger.debug("create session_id: " + newSessionId);
    return newSessionId;
  }

  @Override
  public void remove(String sessionID) {
    S._assert(sessionID);
    mapCache.remove(sessionID);
  }

  @Override
  public void update(String sessionID, Map map) {
    logger.debug("update store for: " + sessionID);
    mapCache.put(sessionID, map);
  }

  @Override
  public Map<String, Map> all() {
    return mapCache.asMap();
  }
}
