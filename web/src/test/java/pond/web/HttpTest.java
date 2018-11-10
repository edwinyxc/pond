package pond.web;

import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.CompleteFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pond.common.PATH;
import pond.common.S;
import pond.common.STREAM;
import pond.common.f.Callback;
import pond.common.f.Tuple;
import pond.net.NetServer;
import pond.net.Server;
import pond.web.http.CtxHttp;
import pond.web.http.HttpConfigBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class HttpTest {

    HttpConfigBuilder builder = new HttpConfigBuilder();
    HttpClient client = HttpClient.newHttpClient();
    Server server;
    HttpRequest request;
    URI uri;

    @Before
    public void init() throws InterruptedException, URISyntaxException {
        server = new NetServer(builder);
        builder.port(9090);
        server.listen();
        uri = new URI("http://localhost:9090/");
        this.request = HttpRequest.newBuilder().GET().uri(uri).build();
        System.setProperty("file.encoding", "utf8");
    }

    @Test
    public void test() throws Exception {
        test_ok();
        test_sendFile();
        test_partialWrite();
        test_cookie();
        //test_multipart();
        server.stop();
    }
    // setup the factory: here using a mixed memory/disk based on size threshold
    HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);


    void test_cookie() throws IOException, InterruptedException {
        builder.clean();
        builder.handler(http -> {
            var ctx = (CtxHttp.Cookies) http::bind;
            Cookie sessionId = ctx.cookie("session-id");
            Assert.assertNotNull(sessionId);
            sessionId.setValue(sessionId.value() + "_XXX");
            var cookieStr = ServerCookieEncoder.STRICT.encode(sessionId);
            S.echo("Cookie from Server", cookieStr);
            http.response()
                .header("Set-Cookie", cookieStr);
        });
        HttpRequest req =
            HttpRequest.newBuilder(uri)
                .GET()
                .header("Cookie", ClientCookieEncoder.STRICT.encode("session-id", "XXX") )
                .build();
        var cookie_str = client.send(req, HttpResponse.BodyHandlers.ofString()).headers().firstValue("Set-Cookie")
                          .orElse("Error");
        S.echo("Cookie Client Got", cookie_str);
        var cookie = ClientCookieDecoder.STRICT.decode(cookie_str);
        Assert.assertEquals("XXX_XXX", cookie.value());
    }

    public void test_partialWrite() throws IOException, InterruptedException {
        builder.clean();
        builder.handlers(List.of(
            ctx -> {
                var bind = (CtxHttp & CtxHttp.Send) ctx::bind;
                bind.response(HttpResponseStatus.OK);
            },
            ctx -> {
                ctx.response().headers().set("AAAA", "AAAA");
                ctx.response().write("OK");
            },
            ctx -> {
                ctx.response().write("KO");
            }
        ));

        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assert.assertEquals("OKKO", response.body());
        Assert.assertEquals("AAAA", response.headers().firstValue("AAAA").orElse("Error"));
    }

    public void test_sendFile() throws URISyntaxException, IOException, InterruptedException {
        builder.clean();
        builder.handler(sendFile);
        Assert.assertEquals(
            STREAM.readFully(new FileInputStream(new File(PATH.classpathRoot() + "logback.xml")), CharsetUtil.UTF_8)
            ,
            client.send(request, HttpResponse.BodyHandlers.ofString()).body()
        );
    }

    public void test_ok() throws URISyntaxException, IOException, InterruptedException {
        builder.clean();
        builder.handler(ok);
        Assert.assertEquals("OK", client.send(request, HttpResponse.BodyHandlers.ofString()).body());
    }

    public static CtxHandler<CtxHttp> ok = ctx -> {
        //print all headers
        S.echo("headers", ((CtxHttp.Headers) ctx::bind).all());
        ((CtxHttp.Send) ctx::bind).Ok(CtxHttp.str("OK"));
    };

    public static CtxHandler<CtxHttp> sendFile = ctx -> {
        File file = new File(PATH.classpathRoot() + "logback.xml");
        //File file = new File("C:\\Users\\PC\\Downloads\\BaseItems_V1.zip");//big
        S.echo("Send File", file.getAbsolutePath());
        var send = (CtxHttp.Send) ctx::bind;
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
        var send = (CtxHttp.Send) ctx::bind;
        send.send(
            ctx.response(HttpResponseStatus.OK)
                .write("Hello").build()
        );
    };
}
