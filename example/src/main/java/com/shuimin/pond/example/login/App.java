package com.shuimin.pond.example.login;

import com.shuimin.pond.codec.view.View;
import com.shuimin.pond.core.Pond;
import com.shuimin.pond.core.mw.Action;
import com.shuimin.pond.core.mw.Dispatcher;
import com.shuimin.pond.core.spi.Router;

import static com.shuimin.pond.core.Interrupt.render;

/**
 * Created by ed on 2014/4/10.
 */
public class App {
    public static void main(String[] args) {
        Pond.init().use(
           new Dispatcher()
               .get("/", index)
               .post("/login", Service.parseUser, Service.checkPass, Service.showResult)
       ).start(10000);

    }

    public static Action index = Action.fly(() -> render(View.Text.one().val(
        "<h1>Welcome</h1>"
            + "<form action='login' method='post' >"
            + "<p>ID:<input type='text' name='id'></p>"

            + "<p>PASS:<input type='password' name='pass'></p>"
            + "<p><input type='submit'></p>"
            + "</form>"
    )));

}
