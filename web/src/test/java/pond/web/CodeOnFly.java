package pond.web;

import io.netty.buffer.Unpooled;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.HttpResponseStatus;
import pond.common.PATH;
import pond.common.S;
import pond.net.NetServer;
import pond.web.http.CtxHttp;
import pond.web.http.HttpConfigBuilder;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CodeOnFly {

    public static CtxHandler<CtxHttp> ok =  ctx -> {
        //print all headers
        S.echo("headers", ((CtxHttp.Headers) ctx::bind).all());
        ((CtxHttp.Send) ctx::bind).Ok(CtxHttp.str("OK"));
    };

    public static CtxHandler<CtxHttp> sendFile = ctx -> {
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

    public static CtxHandler<CtxHttp> blocking = ctx -> {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    };

    public static CtxHandler<CtxHttp> send = ctx -> {
        var send = (CtxHttp.Send)ctx::bind;
        send.send(
            ctx.response(HttpResponseStatus.OK)
            .write("Hello").build()
        );
    };


    public static void main(String[] args) throws ExecutionException, InterruptedException {
        S._debug_on(NetServer.class);
        new NetServer(
            new HttpConfigBuilder().handlers(
                List.of(ok)
            ).boosGroup(() -> new NioEventLoopGroup(1))
                .workerGroup(() -> new NioEventLoopGroup(2))
            .port(8333)
        ).listen().get();
    }
}
