package pond.web;

import pond.common.S;
import pond.common.SPILoader;
import pond.common.f.Function;
import pond.web.http.Cookie;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ed on 9/7/15.
 */
public class Session {

    static SessionStore store = new DefaultSessionStore();

    public static SessionStore store() {
        return store;
    }

    final Map map;
    final String id;

    Session(String id, Map map) {
        this.id = id;
        this.map = map;
    }

    public String id() {
        return id;
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

    static class CookieSessionInstaller implements Mid {

        final int maxAge;

        /**
         * default to a year
         */
        CookieSessionInstaller() {
            this.maxAge = 3600 * 365 * 24;
        }

        CookieSessionInstaller(int maxAge) {
            this.maxAge = maxAge;
        }

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
                cookie.setMaxAge(maxAge);
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

    static class SessionInstaller implements Mid {

        final Function<String, Request> hook_session_id;
        final Mid cb_onFailed;

        SessionInstaller(Function<String, Request> how_to_get_sessionId, Mid callback_on_fail) {
            hook_session_id = how_to_get_sessionId;
            cb_onFailed = callback_on_fail;
        }

        @Override
        public void apply(Request req, Response resp) {
            String sessionid;
            Map data;

            sessionid = hook_session_id.apply(req);
            if (sessionid == null
                    || sessionid.isEmpty()
                    || (data = store.get(sessionid)) == null) {

                cb_onFailed.apply(req, resp);
                return;
            }

            req.ctx().put(LABEL_SESSION, new Session(sessionid, data));
        }
    }

    /**
     * Cookie session installer, using "cookie" & "set-cookie" to control the session-id
     * Put this express into responsibility chain and you will get fully
     * session support then.
     */
    public static Mid install() {
        return new CookieSessionInstaller();
    }

    /**
     * use Cookie installer,
     * @param maxAge cookie max-age in minutes
     * @return
     */
    public static Mid install(int maxAge) {
        return new CookieSessionInstaller(maxAge);
    }

    public static Mid install(Function<String, Request> how_to_get_sessionId, Mid callback_on_fail) {
        return new SessionInstaller(how_to_get_sessionId, callback_on_fail);
    }

    /**
     * Get session from req
     */
    public static Session get(Request request) {
        return get(request.ctx());
    }


    public static Session get(HttpCtx ctx) {

        Session ret = (Session) ctx.get(LABEL_SESSION);
        if (ret == null)
            throw new NullPointerException("Use Session#cookieSession first.");
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Session session = (Session) o;

        if (!id.equals(session.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Session{" +
                "map=" + map +
                ", id='" + id + '\'' +
                '}';
    }
}
