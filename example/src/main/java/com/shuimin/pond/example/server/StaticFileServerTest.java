package com.shuimin.pond.example.server;

import com.shuimin.pond.core.Pond;
import com.shuimin.pond.core.mw.Action;
import com.shuimin.pond.core.mw.StaticFileServer;

import static com.shuimin.pond.core.Pond.RESP;

/**
 * Created by ed on 2014/4/11.
 */
public class StaticFileServerTest {

    public static void main(String[] args) {
        Pond.init().debug()
                .use(Action.fly(() -> RESP().contentType("text/html;charset=utf-8")))
                .use(new StaticFileServer("C:\\var\\www")
                                .defaultPages("index.html", "fine.html")
                ).start(10000);
    }

}
