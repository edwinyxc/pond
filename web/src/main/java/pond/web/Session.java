package pond.web;

import pond.common.S;
import pond.common.SPILoader;
import pond.web.http.Cookie;
import pond.web.spi.SessionStore;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ed on 9/7/15.
 */
public class Session {

  static SessionStore store = SPILoader.service(SessionStore.class);

  final Map map;
  final String id;

  Session(String id, Map map) {
    this.id = id;
    this.map = map;
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

  public void invalidate() {
    store.remove(id);
  }

  public static String LABEL_SESSION = "x-pond-sessionid";

  static class SessionInstaller implements Mid {

    @Override
    public void apply(Request req, Response resp) {

      Cookie cookie;
      String sessionid;
      Map data;

      cookie = req.cookie(LABEL_SESSION);

      if (cookie == null) {
        //totally new
        sessionid = store.create(data = new HashMap());
        cookie = new Cookie(LABEL_SESSION, sessionid);
        //TODO
        cookie.setMaxAge(1800);
      } else {
        sessionid = cookie.getValue();

        data = store.get(sessionid);
        if (data == null) {
          cookie.setMaxAge(0);
          data = new HashMap();
        }
      }

      //new a session each time
      req.ctx().put(LABEL_SESSION, new Session(sessionid, data));

      //avoid different paths
      cookie.setPath("/");
      //now the user should be able to use the session
      resp.cookie(cookie);
    }
  }

  /**
   * Put this mid into responsibility chain and you will get fully
   * session support then.
   */
  public static Mid install = new SessionInstaller();


  /**
   * Get session from req
   */
  public static Session get(Request request) {
    return get(request.ctx());
  }


  public static Session get(Ctx ctx) {

    Session ret = (Session) ctx.get(LABEL_SESSION);
    if (ret == null)
      throw new NullPointerException("Use Session#install first.");
    return ret;
  }

}
