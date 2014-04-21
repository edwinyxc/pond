package com.shuimin.jtiny.codec.session;

import com.shuimin.base.S;
import com.shuimin.base.f.Function;
import com.shuimin.jtiny.core.Server;
import com.shuimin.jtiny.core.misc.Makeable;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.shuimin.jtiny.core.ExecutionContext.CUR;
import static com.shuimin.jtiny.core.Server.G.debug;

/**
 * Created by ed on 2014/4/18.
 */
public class SessionManager implements Makeable<SessionManager> {

    private final static Map<String, SessionWatcher> sessions = new HashMap<>();

    public static final String CHECK_INTERVAL = "SessionInstaller.check_interval";

    public static final String INVALID_INTERVAL = "SessionInstaller.invalid_interval";

    public static int CHECK_INTERVAL_SECONDS_DEFAULT = 60 * 30;

    public static int INVALID_INTERVAL_SECONDS_DEFAULT = 60 * 30;

    public static Timer watcherTimer = new Timer(true);

    {
        Server.config(CHECK_INTERVAL, CHECK_INTERVAL_SECONDS_DEFAULT);
        Server.config(INVALID_INTERVAL, INVALID_INTERVAL_SECONDS_DEFAULT);
    }

    private static int invalidIntervalSeconds() {
        return (Integer) Server.config(INVALID_INTERVAL);
    }

    private static int checkIntervalSeconds() {
        return (Integer) Server.config(CHECK_INTERVAL);
    }

    public static Session get(String sessionId) {
        return get(sessionId, true);
    }

    public static Session get(String sessionId, boolean createOnNotFound) {
        SessionWatcher ret;
        synchronized (sessions) {
            ret = sessions.get(sessionId);
        }
        if (ret == null && createOnNotFound) {
            ret = new SessionWatcher(supplierFunc.apply(sessionId));
            synchronized (sessions) {
                sessions.put(sessionId, ret);
            }
            watcherTimer.schedule(ret, 0, checkIntervalSeconds() * 1000);
        }
        debug("got session : " + ret.session.id());
        return ret.session;
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

    private static Function<Session, String> supplierFunc = (id) -> {
        debug("session : "+id+" created");
        return new Session(id);
    };

//    public static

    /**
     * <p>快捷获得session，如果没有使用 SessionInstaller则可能会出现意想不到的情况</p>
     *
     * @return
     */
    public static Session get() {
        return get(CUR().attr(SessionInstaller.JSESSIONID));
    }

    public static SessionInstaller installer() {
        return new SessionInstaller();
    }


    private static class SessionWatcher extends TimerTask {
        Session session;

        public SessionWatcher(Session s) {
            session = s;
        }

        @Override
        public void run() {
            long time = S.time();
            if ((time - session.lastActiveTime())
                > invalidIntervalSeconds() * 1000) {
                synchronized (sessions) {
                    kill(session);
                }
                this.cancel();
                debug(session.id() + " has been invalidated.");
            } else {
                debug(session.id() + " holding");
            }
        }

        public Session session() {
            return this.session;
        }
    }


}
