package com.shuimin.pond.codec.session;

import com.shuimin.common.f.Function;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import static com.shuimin.pond.core.Pond.debug;


/**
 * Created by ed on 2014/4/22.
 */
public class TimerSession extends HashMap<String, Object>
        implements Session {

    private final Timer timer;

    private final String id;
    private final Function.F0<Integer> lifetimeProvider;
    private TimerTask suicide = new Suicide();

    public TimerSession(String id,
                        Function.F0<Integer> lifetimeProvider) {
        this.id = id;
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
        return id;
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
                "id='" + id + '\'' +
                '}';
    }

    private class Suicide extends TimerTask {

        @Override
        public void run() {
            TimerSession session = TimerSession.this;
            SessionManager.kill(session);
            debug(session + "invalidated.");
        }

    }
}
