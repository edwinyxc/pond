package com.shuimin.pond.example.server;

import com.shuimin.pond.codec.StaticFileServer;
import com.shuimin.pond.core.Server;
import com.shuimin.pond.core.mw.Action;

import static com.shuimin.pond.core.ExecutionContext.RESP;

/**
 * Created by ed on 2014/4/11.
 */
public class StaticFileServerTest {

    public static void main(String[] args) {
        Server.basis(Server.BasicServer.jetty).debug()
            .use(Action.fly(() -> RESP().contentType("text/html;charset=utf-8")))
            .use(new StaticFileServer("C:\\var\\www")
                    .defaultPages("index.html", "fine.html")
            ).listen(10000);
    }

}
