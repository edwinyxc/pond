package com.shuimin.pond.example.FileUpload;

import com.shuimin.pond.codec.upload.FileUploadServer;
import com.shuimin.pond.core.Global;
import com.shuimin.pond.core.Middleware;
import com.shuimin.pond.core.Pond;
import com.shuimin.pond.core.mw.Action;
import com.shuimin.pond.core.mw.Dispatcher;
import com.shuimin.pond.core.mw.StaticFileServer;
import com.shuimin.pond.core.mw.router.Router;

import static com.shuimin.common.S.echo;
import static com.shuimin.pond.core.Interrupt.render;
import static com.shuimin.pond.core.Renderable.text;

/**
 * Created by ed on 2014/4/22.
 */
public class App {

    public static void main(String[] args) {
        Dispatcher app = new Dispatcher(Router.regex());
        app.get("/index",index);
        app.post("/upload", upload);
        app.get(".*", new StaticFileServer("C:\\var\\www"));
        Pond.init().use(app).start(8080);
        echo("ROOT: " + Pond.get().attr(Global.ROOT));
    }

    public static Action index = Action.simple((req,resp) -> {
    String html = "<html><body><h1>333</h1><form method = 'post' action = 'upload' enctype = 'multipart/form-data' >" +
            "<input type = 'file' name= 'test'>" +
            "<input type = submit> </form></body></html>";
        render(text(html));
    });

    public static Middleware upload =  new FileUploadServer();
}
