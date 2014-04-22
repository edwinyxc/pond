package com.shuimin.pond.example.session;

import com.shuimin.pond.codec.StaticFileServer;
import com.shuimin.pond.codec.session.SessionInstaller;
import com.shuimin.pond.codec.session.SessionManager;
import com.shuimin.pond.core.Dispatcher;
import com.shuimin.pond.core.Server;
import com.shuimin.pond.core.mw.Action;
import com.shuimin.pond.core.mw.router.Router;

import static com.shuimin.common.S._notNullElse;
import static com.shuimin.pond.core.ExecutionContext.CUR;
import static com.shuimin.pond.core.Interrupt.render;
import static com.shuimin.pond.core.Server.G.debug;
import static com.shuimin.pond.core.misc.Renderable.text;

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
        app.get("/get", Action.fly(() ->
                render(text(_notNullElse((String) SessionManager.get().get("username"),
                    "not set")))
        ));
        app.get("/session", Action.fly(() ->
            render(text("session id :" +CUR().attr(SessionInstaller.JSESSIONID)))));


        Server.G.mode(Server.RunningMode.debug);


        debug("welcome");

        Server.basis(Server.BasicServer.jetty).use(SessionManager
            .installer())
            .use(app).use(new StaticFileServer("C:\\var\\www")).listen(8080);

    }


}