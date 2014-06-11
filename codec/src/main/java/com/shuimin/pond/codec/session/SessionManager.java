package com.shuimin.pond.codec.session;

import com.shuimin.common.abs.Makeable;
import com.shuimin.common.f.Function;
import com.shuimin.pond.core.Pond;

import java.util.HashMap;
import java.util.Map;

import static com.shuimin.pond.core.Pond.CUR;
import static com.shuimin.pond.core.Pond.debug;


/**
 * Created by ed on 2014/4/18.
 */
public class SessionManager implements Makeable<SessionManager> {

    public static final String SESSION_LIFETIME = "session_lifetime";
    private final static Map<String, Session> sessions = new HashMap<>();
    private static Function<Session, String> supplierFunc = (id) -> {
        debug("session : " + id + " created");
        return new TimerSession(id, SessionManager::sessionLifeTime);
    };

    public static Session get(String sessionId) {
        return get(sessionId, true);
    }

    public static Session get(String sessionId, boolean createOnNotFound) {
        Session ret;
        synchronized (sessions) {
            ret = sessions.get(sessionId);
        }
        if (ret == null && createOnNotFound) {
            ret = supplierFunc.apply(sessionId);
            synchronized (sessions) {
                sessions.put(sessionId, ret);
            }
        }
        if (ret != null)
            debug("got session : " + ret.toString());
        return ret;
    }

    public static void kill(Session session) {
        synchronized (sessions) {
            sessions.remove(session.id());
        }
    }

    public static void kill(String sessionId) {
        synchronized (sessions) {
            sessions.remove(sessionId);
        }
    }

    private static int sessionLifeTime() {
        return (Integer) Pond.get().attr(
                SESSION_LIFETIME) * 1000;
    }

//    public static

    /**
     * <p>快捷获得session，如果没有使用 SessionInstaller则可能会出现意想不到的情况</p>
     *
     * @return 当前的session
     */
    public static Session get() {
        return get((String) CUR().attr(SessionInstaller.JSESSIONID));
    }


    public static SessionInstaller installer() {
        return new SessionInstaller();
    }

}
