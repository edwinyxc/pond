package pond.core.session;

import pond.common.abs.Makeable;
import pond.common.f.Function;
import pond.core.Ctx;
import pond.core.Pond;
import pond.core.Session;

import java.util.HashMap;
import java.util.Map;

import static pond.core.Pond.debug;


/**
 * Created by ed on 2014/4/18.
 */
public class SessionManager implements Makeable<SessionManager> {

    public static final String SESSION_LIFETIME = "SessionManager.session_lifetime";
    public static final int default_life_time = 60 * 30;
    final Pond pond;
    private final Map<String, Session> sessions = new HashMap<>();
    private Function<Session, String> supplierFunc = (id) -> {
        debug("session : " + id + " created");
        return new TimerSession(this, id, this::sessionLifeTime);
    };

    public SessionManager(Pond pond) {
        this.pond = pond;
    }

    public Session get(String sessionId) {
        return get(sessionId, true);
    }

    public Session get(String sessionId, boolean createOnNotFound) {
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

    public void kill(Session session) {
        synchronized (sessions) {
            sessions.remove(session.id());
        }
    }

    public void kill(String sessionId) {
        synchronized (sessions) {
            sessions.remove(sessionId);
        }
    }

    private int sessionLifeTime() {
        // as mills-seconds
        String sessionLifeStr = (String) pond.config.get(SESSION_LIFETIME);

        return  Integer.parseInt(sessionLifeStr) * 1000 ;
    }

//    public static

    /**
     * <p>快捷获得session，如果没有使用 SessionInstaller则可能会出现意想不到的情况</p>
     *
     * @return 当前的session
     */
    public Session get(Ctx ctx) {
        return get((String) ctx.get(SessionInstaller.JSESSIONID));
    }


    public SessionInstaller installer() {
        return new SessionInstaller(this);
    }

}
