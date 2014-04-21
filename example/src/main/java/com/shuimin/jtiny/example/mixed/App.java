package com.shuimin.jtiny.example.mixed;

import com.shuimin.jtiny.codec.StaticFileServer;
import com.shuimin.jtiny.codec.session.SessionInstaller;
import com.shuimin.jtiny.codec.session.SessionManager;
import com.shuimin.jtiny.core.Dispatcher;
import com.shuimin.jtiny.core.Server;
import com.shuimin.jtiny.core.mw.Action;
import com.shuimin.jtiny.core.mw.router.Router;

import static com.shuimin.base.S._notNullElse;
import static com.shuimin.jtiny.core.ExecutionContext.CUR;
import static com.shuimin.jtiny.core.Interrupt.render;
import static com.shuimin.jtiny.core.Server.G.debug;
import static com.shuimin.jtiny.core.misc.Renderable.text;

public class App {
    public static void main(String[] args) {

        Dispatcher app = new Dispatcher(Router.regex());

        app.get("/index.html", Action.fly(() -> {
                SessionManager.get().set("username","hello");
                render(text("hello"));
            }
        ));
        app.get("/set",Action.fly(() -> {
                SessionManager.get().set("username","hello");
                render(text("hello"));
            }
        ));
        app.get("/get", Action.fly(() -> {
                render(text(_notNullElse((String) SessionManager.get().get("username"),
                    "not set")));
            }
        ));
        app.get("/session", Action.fly(() ->
            render(text("session id :" +CUR().attr(SessionInstaller.JSESSIONID)))));


        Server.global().mode(Server.RunningMode.debug);

        Server.config(SessionManager.CHECK_INTERVAL,30*3600);
        Server.config(SessionManager.INVALID_INTERVAL,30*3600);

        debug("welcome");

        Server.basis(Server.BasicServer.jetty).use(SessionManager.installer())
            .use(app).use(new StaticFileServer("C:\\var\\www")).listen(8080);

    }


}