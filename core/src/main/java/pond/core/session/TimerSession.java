package pond.core.session;

import pond.common.f.Function;
import pond.core.Session;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import static pond.common.f.Function.F0;
import static pond.core.Pond.debug;


/**
 * Created by ed on 2014/4/22.
 */
public class TimerSession extends HashMap<String, Object>
        implements Session {

    private final Timer timer;

    private final Function.F0<Integer> lifetimeProvider;
    private TimerTask suicide = new Suicide();
    private SessionManager mgr;

    public TimerSession(SessionManager mgr,
                        String         id,
                        F0<Integer>    lifetimeProvider) {
        this.mgr = mgr;
        super.put("id", id);
        this.timer = new Timer();
        this.lifetimeProvider = lifetimeProvider;
        timer.schedule(suicide, lifetimeProvider.apply());
    }

    private synchronized void reschedule() {
        suicide.cancel();
        suicide = new Suicide();
        debug("delay time =" + lifetimeProvider.apply());
        timer.schedule(suicide, lifetimeProvider.apply());
        debug(this.toString() + " has been rescheduled");
    }

    @Override
    public String id() {
        return String.valueOf(super.get("id"));
    }

    @Override
    public Object get(String key) {
        reschedule();
        return super.get(key);
    }

    @Override
    public Session set(String key, Object value) {
        reschedule();
        super.put(key, value);
        return this;
    }

    @Override
    public String toString() {
        return "TimerSession{" +
                "id='" + id()+ '\'' +
                '}';
    }

    private class Suicide extends TimerTask {

        @Override
        public void run() {
            TimerSession session = TimerSession.this;
            mgr.kill(session);
            debug(session + "invalidated.");
        }

    }
}
