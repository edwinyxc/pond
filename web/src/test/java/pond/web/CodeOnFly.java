package pond.web;

import pond.common.S;
import pond.core.Context;
import pond.core.CtxBase;
import pond.net.NetServer;
import pond.web.http.CtxHttp;
import pond.web.http.HttpConfigBuilder;

import java.util.concurrent.ExecutionException;

public class CodeOnFly {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        new NetServer(
            new HttpConfigBuilder()
            .handler(ctx -> {
                //Context base = ctx.delegate();
                //print all headers
                S.echo("headers", ((CtxHttp.Headers) ctx::delegate).all());
                ((CtxHttp.Send)ctx::delegate).ok();
            })
            .port(8333)
        ).listen().get();
    }
}
