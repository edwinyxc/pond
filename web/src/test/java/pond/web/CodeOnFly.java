package pond.web;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponseStatus;
import pond.common.PATH;
import pond.common.S;
import pond.net.NetServer;
import pond.web.http.CtxHttp;
import pond.web.http.HttpConfigBuilder;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class CodeOnFly {

    public static CtxHandler ok =  ctx -> {
        //print all headers
        S.echo("headers", ((CtxHttp.Headers) ctx::bind).all());
        ((CtxHttp.Send) ctx::bind).Ok(CtxHttp.str("OK"));
    };

    public static CtxHandler sendFile = ctx -> {
        File file = new File(PATH.classpathRoot() + "logback.xml");
        //File file = new File("C:\\Users\\PC\\Downloads\\BaseItems_V1.zip");//big
        S.echo("Send File" , file.getAbsolutePath());
        var send = (CtxHttp.Send)ctx::bind;
        try {
            send.sendFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    };

    public static CtxHandler

    public static CtxHandler send = ctx -> {
        var send = (CtxHttp.Send)ctx::bind;
        send.send(
            ctx.response(HttpResponseStatus.OK)
            .write("Hello")
            .build()
        );
    };


    public static void main(String[] args) throws ExecutionException, InterruptedException {
        S._debug_on(NetServer.class);
        new NetServer(
            new HttpConfigBuilder().handler(sendFile).port(8333)
        ).listen().get();
    }
}
