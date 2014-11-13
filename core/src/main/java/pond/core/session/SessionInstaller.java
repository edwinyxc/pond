package pond.core.session;

import pond.common.S;
import pond.common.f.Callback;
import pond.core.Mid;
import pond.core.Pond;
import pond.core.Request;
import pond.core.Response;

import javax.servlet.http.Cookie;

/**
 * Created by ed on 2014/4/21.
 * <p>一个模拟tomcat session实现，既在用户访问页面的时候检查
 * Cookie[JSESSIONID] 若没有则加入一个session </p>
 */
public class SessionInstaller
        implements Mid {

    public static final String JSESSIONID = "JSESSIONID";
    Pond pond;

    public SessionInstaller(SessionManager mgr) {
        this.pond = mgr.pond;
        String time;
        if( (time = (String) pond.config.get( SessionManager.SESSION_LIFETIME )) == null ) {
            time = String.valueOf( SessionManager.default_life_time );
        }
        //set life time
        pond.config.put( SessionManager.SESSION_LIFETIME, time );
    }


    String checkJSession(Request req) {
        Cookie c = req.cookie(JSESSIONID);
        if (c == null) return null;
        return c.getValue();
    }

    String writeSessionId(Response resp) {
        String uuid = S.uuid.vid();
        Cookie c = new Cookie(JSESSIONID, uuid);
        resp.cookie(c);
        return uuid;
    }

    public SessionInstaller maxLifeSec(int seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException("invalid seconds");
        }
        //set life time
        pond.config.put( SessionManager.SESSION_LIFETIME, String.valueOf( seconds ));
        return this;
    }


    @Override
    public void apply(Request request, Response response, Callback.C0 next) {
        String uuid;
        if (null == (uuid = checkJSession(request))) {
            uuid = writeSessionId(response);
            response.redirect(request.path());
        }
        request.ctx().put(JSESSIONID, uuid);
        next.apply();
    }
}
