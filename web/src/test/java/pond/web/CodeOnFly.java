package pond.web;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.HttpResponseStatus;
import pond.common.PATH;
import pond.common.S;
import pond.core.CtxFlowProcessor;
import pond.core.CtxHandler;
import pond.net.NetServer;
import pond.web.http.HttpCtx;
import pond.web.http.HttpConfigBuilder;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class CodeOnFly {

    public static CtxHandler<HttpCtx> ok = ctx -> {
        //print all headers
        S.echo("headers", ((HttpCtx.Headers) ctx::bind).all());
        ((HttpCtx.Send) ctx::bind).Ok(HttpCtx.str("OK"));
    };

    public static CtxHandler<HttpCtx> sendFile = ctx -> {
        File file = new File(PATH.classpathRoot() + "logback.xml");
        //File file = new File("C:\\Users\\PC\\Downloads\\BaseItems_V1.zip");//big
        S.echo("Send File" , file.getAbsolutePath());
        var send = (HttpCtx.Send)ctx::bind;
            send.send(file);
    };

    public static CtxHandler<HttpCtx> blocking = ctx -> {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    };

    public static CtxHandler<HttpCtx> send = ctx -> {
        var send = (HttpCtx.Send)ctx::bind;
        send.send(
            ctx.response(HttpResponseStatus.OK)
            .write("Hello").build()
        );
    };


    public static void main(String[] args) throws ExecutionException, InterruptedException {
        S._debug_on(NetServer.class);
        var fixedThPool = Executors.newFixedThreadPool(4);
        var heavyJobProcessor = new CtxFlowProcessor("heavy").executor(fixedThPool);

        new NetServer(
            new HttpConfigBuilder().handlers(
                List.of(
                    blocking.flowTo(heavyJobProcessor),
                    ok
                )
            ).boosGroup(() -> new NioEventLoopGroup(1))
                .workerGroup(() -> new NioEventLoopGroup(2))
            .port(8333)
        ).listen().get();
    }
}
