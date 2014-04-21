package com.shuimin.jtiny.example.login;

import com.shuimin.jtiny.codec.view.View;
import com.shuimin.jtiny.core.Server;
import com.shuimin.jtiny.core.mw.Action;
import com.shuimin.jtiny.core.Dispatcher;
import com.shuimin.jtiny.core.mw.router.Router;

import static com.shuimin.jtiny.core.Interrupt.render;

/**
 * Created by ed on 2014/4/10.
 */
public class App {
    public static void main(String[] args) {
        Server.basis(Server.BasicServer.jetty).use(
            new Dispatcher(Router.regex())
                .get("/", index)
                .post("/login", Service.parseUser, Service.checkPass, Service.showResult)
        ).listen(10000);

    }

    public static Action index = Action.fly(() -> render(View.Text.one().text(
        "<h1>Welcome</h1>"
            + "<form action='login' method='post' >"
            + "<p>ID:<input type='text' name='id'></p>"

            + "<p>PASS:<input type='password' name='pass'></p>"
            + "<p><input type='submit'></p>"
            + "</form>"
    )));

}
