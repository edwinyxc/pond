package pond.web;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import pond.common.PATH;
import pond.common.S;
import pond.core.CtxFlowProcessor;
import pond.core.CtxHandler;
import pond.net.NetServer;
import pond.web.http.HttpCtx;
import pond.web.http.HttpConfigBuilder;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class CodeOnFly {

    public static CtxHandler<HttpCtx> ok = ctx -> {
        //print headers headers
        S.echo("headers", ((HttpCtx.Headers) ctx::bind).headers());
        ((HttpCtx.Send) ctx::bind).sendOk(HttpCtx.str("OK"));
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

    public static CtxHandler<HttpCtx> addTwoCookies = ctx -> {
        var http = (HttpCtx.Send & HttpCtx.Cookies)ctx::bind;
        http.removeCookie( http.cookie("pond"));
        http.addCookie(new DefaultCookie("a", "aaa"))
            .addCookie(new DefaultCookie("b", "bbb"));
        http.response().write("All set");
    };


    public static void main(String[] args) throws ExecutionException, InterruptedException {
        S._debug_on(NetServer.class);
        var fixedThPool = Executors.newFixedThreadPool(4);
        var heavyJobProcessor = new CtxFlowProcessor("heavy").executor(fixedThPool);

        new NetServer(
            new HttpConfigBuilder().handler(
                addTwoCookies
            ).boosGroup(() -> new NioEventLoopGroup(1))
                .workerGroup(() -> new NioEventLoopGroup(2))
            .port(8333)
        ).listen().get();
    }
}
