package com.shuimin.jtiny.example.FileUpload;

import com.shuimin.jtiny.codec.StaticFileServer;
import com.shuimin.jtiny.codec.upload.FileUploadServer;
import com.shuimin.jtiny.core.Dispatcher;
import com.shuimin.jtiny.core.Global;
import com.shuimin.jtiny.core.Middleware;
import com.shuimin.jtiny.core.Server;
import com.shuimin.jtiny.core.mw.Action;
import com.shuimin.jtiny.core.mw.router.Router;

import static com.shuimin.base.S.echo;
import static com.shuimin.jtiny.core.Interrupt.render;
import static com.shuimin.jtiny.core.misc.Renderable.text;

/**
 * Created by ed on 2014/4/22.
 */
public class App {
    public static void main(String[] args) {
        Dispatcher app = new Dispatcher(Router.regex());
        app.get("/index",index);
        app.post("/upload", upload);
        app.get(".*", new StaticFileServer("C:\\var\\www"));
        Server.G.mode(Server.RunningMode.debug);
        Server.basis(Server.BasicServer.jetty).use(app).listen(8080);
        echo("ROOT: "+ Server.config(Global.ROOT));
    }

    public static Action index = Action.simple((req,resp) -> {
    String html = "<html><body><h1>333</h1><form method = 'post' action = 'upload' enctype = 'multipart/form-data' >" +
            "<input type = 'file' name= 'test'>" +
            "<input type = submit> </form></body></html>";
        render(text(html));
    });

    public static Middleware upload =  new FileUploadServer();
}
