package com.shuimin.jtiny.codec.session;

import com.shuimin.base.S;
import com.shuimin.jtiny.core.AbstractMiddleware;
import com.shuimin.jtiny.core.ExecutionContext;
import com.shuimin.jtiny.core.http.Request;
import com.shuimin.jtiny.core.http.Response;

import javax.servlet.http.Cookie;

import static com.shuimin.jtiny.core.ExecutionContext.CUR;
import static com.shuimin.jtiny.core.Interrupt.redirect;

/**
 * Created by ed on 2014/4/21.
 * <p>一个模拟tomcat session实现，既在用户访问页面的时候检查
 * Cookie[JSESSIONID] 若没有则加入一个session </p>
 */
public class SessionInstaller extends AbstractMiddleware{

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
