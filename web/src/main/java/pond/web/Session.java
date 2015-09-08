package pond.web;

import pond.common.S;
import pond.common.SPILoader;
import pond.web.spi.SessionStore;

import java.util.Collections;
import java.util.Map;

/**
 * Created by ed on 9/7/15.
 */
public class Session {

  static SessionStore store = SPILoader.service(SessionStore.class);

  final Map map;
  final String id;

  Session(String id) {
    this.id = id;
    if ((map = store.get(id)) == null)
      throw new NullPointerException("Cannot fetch session for id: " + id);
  }

  @SuppressWarnings("unchecked")
  public <E> E get(String name) {
    return (E) map.get(name);
  }

  @SuppressWarnings("unchecked")
  public <E> E getOrDefault(String name, E e) {
    return (E) map.getOrDefault(name, e);
  }

  @SuppressWarnings("unchecked")
  public <E> E getOrSet(String name, E e) {
    return (E) S._getOrSet(map, name, e);
  }

  @SuppressWarnings("unchecked")
  public <E> Session set(String name, E e) {
    map.put(name, e);
    return this;
  }

  /**
   * Remember to call this method to write changes back
   */
  public void save() {
    store.update(id, map);
  }

  public static String LABEL_SESSION = "x-pond-sessionid";

  /**
   * Put this mid into responsibility chain and you will get fully
   * session support then.
   */
  public static Mid install = (req, resp) -> {
    String sessionid = req.header(LABEL_SESSION);

    if(sessionid == null)
      sessionid = store.create(Collections.emptyMap());

    req.ctx().put(LABEL_SESSION, new Session(sessionid));

  };

  /**
   * Get session from req
   */
  public static Session session(Request request){
    Session ret = (Session) request.ctx().get(LABEL_SESSION);
    if(ret == null)
      throw new NullPointerException("use Session#install first.");
    return ret;
  }

}
