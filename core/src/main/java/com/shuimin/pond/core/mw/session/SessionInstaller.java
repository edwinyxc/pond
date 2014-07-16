package com.shuimin.pond.core.mw.session;

import com.shuimin.common.S;
import com.shuimin.common.f.Callback;
import com.shuimin.pond.core.Mid;
import com.shuimin.pond.core.Pond;
import com.shuimin.pond.core.Request;
import com.shuimin.pond.core.Response;

import javax.servlet.http.Cookie;

/**
 * Created by ed on 2014/4/21.
 * <p>一个模拟tomcat session实现，既在用户访问页面的时候检查
 * Cookie[JSESSIONID] 若没有则加入一个session </p>
 */
public class SessionInstaller
        implements Mid {

    public static final String JSESSIONID = "JSESSIONID";
    int life_time = 60 * 30;

    public SessionInstaller() {
        setLifeTime(life_time);
    }

    public void setLifeTime(int i) {
        life_time = i;
        Pond.get().attr(SessionManager.SESSION_LIFETIME, life_time);
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
        setLifeTime(seconds);
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
