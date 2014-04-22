package com.shuimin.pond.codec.session;

import com.shuimin.common.S;
import com.shuimin.pond.core.AbstractMiddleware;
import com.shuimin.pond.core.ExecutionContext;
import com.shuimin.pond.core.Server;
import com.shuimin.pond.core.http.Request;
import com.shuimin.pond.core.http.Response;
import com.shuimin.pond.core.misc.Makeable;

import javax.servlet.http.Cookie;

import static com.shuimin.pond.core.ExecutionContext.CUR;
import static com.shuimin.pond.core.Interrupt.redirect;

/**
 * Created by ed on 2014/4/21.
 * <p>一个模拟tomcat session实现，既在用户访问页面的时候检查
 * Cookie[JSESSIONID] 若没有则加入一个session </p>
 */
public class SessionInstaller extends AbstractMiddleware
    implements Makeable<SessionInstaller>{

    public static final String JSESSIONID = "JSESSIONID";


    String checkJSession(Request req) {
        Cookie c = req.cookie(JSESSIONID);
        if(c == null) return null;
        return c.getValue();
    }

    String writeSessionId(Response resp) {
        String uuid = S.uuid.vid();
        Cookie c = new Cookie(JSESSIONID, uuid);
        resp.cookie(c);
        return uuid;
    }

    int life_time= 60 *30;

    public SessionInstaller maxLifeSec(int seconds) {
        if(seconds < 0 ){
            throw new IllegalArgumentException("invalid seconds");
        }
        this.life_time = seconds;
        return this;
    }


    @Override
    public void init() {

        Server.config(SessionManager.SESSION_LIFETIME, life_time);
    }

    @Override
    public ExecutionContext handle(ExecutionContext ctx) {
        String uuid;
        if(null == (uuid = checkJSession(ctx.req()))){
            uuid = writeSessionId(ctx.resp());
            redirect(ctx.req().path());
        }
        CUR().attr(JSESSIONID, uuid);
        return ctx;
    }

}
