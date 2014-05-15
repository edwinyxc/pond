package com.shuimin.pond.example;

import com.shuimin.common.S;
import com.shuimin.pond.core.Pond;
import com.shuimin.pond.core.mw.Action;
import com.shuimin.pond.core.mw.Dispatcher;
import com.shuimin.pond.core.mw.router.Router;

import static com.shuimin.pond.core.Interrupt.render;
import static com.shuimin.pond.core.Renderable.view;

/**
 * Created by ed on 2014/5/8.
 */
public class MVCTest {

    public static void main(String[] args) {
        Pond.init().use(new Dispatcher(Router.regex())
            .get("/index", Action.fly(()-> {
            render(view("view/abc.tpl", S.map.hashMap(new Object[][]{
                {"name", "yxc"},
                {"full_name", "edwin yxc"}
            })));
        }))).debug().start(8080);
    }

}
