package pond.web.session;

import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import pond.common.S;
import pond.common.f.Callback;
import pond.common.f.Function;
import pond.core.CtxHandler;
import pond.web.DefaultSessionStore;
import pond.web.Mid;
import pond.web.Request;
import pond.web.http.HttpCtx;
import pond.web.router.Router;
import pond.web.router.RouterCtx;

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

    static class CookieSessionInstaller implements CtxHandler<HttpCtx> {

        final int maxAge;

        /**
         * default to about half a year
         */
        CookieSessionInstaller() {
            this.maxAge = 3600 * 180 * 24;
        }

        CookieSessionInstaller(int maxAge) {
            this.maxAge = maxAge;
        }

        @Override
        public void apply(HttpCtx ctx) {
            var http = (HttpCtx.Cookies & HttpCtx.Send)ctx::bind;
            Cookie cookie;
            String sessionid;
            Map data;

            cookie = http.cookie(LABEL_SESSION);

            if (cookie == null) {
                //totally new
                sessionid = store.create(data = new HashMap());
                cookie = new DefaultCookie(LABEL_SESSION, sessionid);
                cookie.setMaxAge(maxAge);
            } else {
                sessionid = cookie.value();

                data = store.get(sessionid);
                if (data == null) {
                    cookie.setMaxAge(0);
                    data = new HashMap();
                }
            }

            //new a session each time
            http.put(LABEL_SESSION, new Session(sessionid, data));

            //avoid different paths
            cookie.setPath("/");
            //now the user should be able to handler the session
            http.addCookie(cookie);
            var routerCtx = (RouterCtx) ctx::bind;
            routerCtx.continueRouting();
        }
    }

    static class SessionInstaller implements CtxHandler<HttpCtx> {

        final Function<String, HttpCtx> SessionIdGetter;
        final Callback<HttpCtx> onFailed;

        SessionInstaller(Function<String, HttpCtx> getter,
                         Callback<HttpCtx> onFailed) {
            SessionIdGetter = getter;
            this.onFailed = onFailed;
        }

        @Override
        public void apply(HttpCtx t) {
            String sessionid;
            Map data;
            sessionid = SessionIdGetter.apply(t);
            if (sessionid == null
                    || sessionid.isEmpty()
                    || (data = store.get(sessionid)) == null) {

                onFailed.apply(t);
                return;
            }
            t.put(LABEL_SESSION, new Session(sessionid, data));
        }
    }

    /**
     * Cookie session installer, using "cookie" & "set-cookie" to control the session-id
     * Put this toCtxHandler into responsibility chain and you will get fully
     * session support then.
     */
    public static CtxHandler<HttpCtx> install() {
        return new CookieSessionInstaller();
    }

    /**
     * handler Cookie installer,
     * @query maxAge cookie max-age in minutes
     * @return
     */
    public static CtxHandler<HttpCtx> install(int maxAge) {
        return new CookieSessionInstaller(maxAge);
    }

    public static CtxHandler<HttpCtx> install(Function<String, HttpCtx> getter, Callback<HttpCtx> on_fail) {
        return new SessionInstaller(
            getter, on_fail
        );
    }

    public static CtxHandler<HttpCtx> install(Function<String, Request> how_to_get_sessionId, Mid callback_on_fail) {
        return new SessionInstaller(
            (ctx -> how_to_get_sessionId.apply(((HttpCtx.Lazy)ctx::bind).req())),
            callback_on_fail.toCtxHandler()
        );
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
