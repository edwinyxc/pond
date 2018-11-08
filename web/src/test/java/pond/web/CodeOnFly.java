package pond.web;

import io.netty.buffer.Unpooled;
import pond.common.S;
import pond.net.NetServer;
import pond.web.http.CtxHttp;
import pond.web.http.HttpConfigBuilder;

import java.util.concurrent.ExecutionException;

public class CodeOnFly {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        new NetServer(
            new HttpConfigBuilder()
            .handler(ctx -> {
                //print all headers
                S.echo("headers", ((CtxHttp.Headers) ctx::bind).all());
                ((CtxHttp.Send) ctx::bind).Ok(CtxHttp.str("OK"));
            })
            .port(8333)
        ).listen().get();
    }
}
