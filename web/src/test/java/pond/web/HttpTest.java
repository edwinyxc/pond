package pond.web;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.*;
import io.netty.util.CharsetUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pond.common.Convert;
import pond.common.PATH;
import pond.common.S;
import pond.common.STREAM;
import pond.core.Context;
import pond.core.Ctx;
import pond.core.CtxHandler;
import pond.net.NetServer;
import pond.net.Server;
import pond.web.http.HttpCtx;
import pond.web.http.HttpConfigBuilder;
import pond.web.router.Router;
import pond.web.router.RouterCtx;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static pond.core.CtxHandler.*;

public class HttpTest {

    HttpConfigBuilder builder = new HttpConfigBuilder().debug();
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
    public void AssertEqualOnBody(String url, String judge) throws IOException, InterruptedException, URISyntaxException {
        HttpRequest req =
            HttpRequest.newBuilder(new URI(url)) .GET() .build();
        var result = client.send(req, HttpResponse.BodyHandlers.ofString()).body();
        Assert.assertEquals(judge, result);
    }

    @Test
    public void test() throws Exception {
        basic();
        basic_ctx();
        basic_router();
        basic_min_group_route();
        basic_unicode();
//
//        //ROUTER
        complex_route();
        test_nested_router();
//
//        //STATIC
//        static_bind_non_root();
//        static_bind_root();
//        static_default_index();
//
//        //RENDER
        default_json_serialization();

        formal_executive_flow();
//        render_text();
//
//        //MULTIPART
//        multipart();
//
//        //CONTROLLER
//        controller_bind_controller();
//        controller_bind_controller_to_root();
//
//        //SESSION
//        session_test();
//        session_custom_test();

        test_reroute();
        test_ok();
        test_sendFile();
        test_partialWrite();
        test_cookie();
        //test_multipart();
        server.stop();
    }

    void test_reroute() throws InterruptedException, IOException, URISyntaxException {
        Router app = new Router();
        app.get("/proxy/debug", consume(Ctx.SELF, ctx -> {
            var rctx = (RouterCtx) ctx::bind;
            rctx.reRoute(app,"/api/13/do_some_thing");
        }));
        app.get("/api/:id/do_some_thing", consume(Ctx.SELF, ctx -> {
            var http = (HttpCtx.Queries & HttpCtx.Send) ctx::bind;
            http.send(http.inUrlParams().get("id").get(0));
        }));
        builder.clean().handler(app);
        AssertEqualOnBody(
            "http://localhost:9090/proxy/debug", "13"
        );
    }


    void formal_executive_flow() throws InterruptedException, IOException, URISyntaxException {
        Ctx.Entry<String> NAME = new Ctx.Entry<>("Name");
        Ctx.Entry<String> VALUE = new Ctx.Entry<>("Value");

        builder.clean().handler(new Router(
            app -> app.get("/*",
                provider(NAME, () -> "edwinyxc@outlook.com"),
                process(NAME, name-> name+" is a fucker", NAME),
                process(VALUE, val -> "Dog, Wung wung wung", VALUE),
                consume(Ctx.SELF, NAME, VALUE, (ctx, _name, _value)->{
                    var http = (HttpCtx.Send)ctx::bind;
                    http.send(new Object(){
                        public String name = _name;
                        public String value = _value;
                    });
                })
            )));
        AssertEqualOnBody(
            "http://localhost:9090/api/evil/a",
            "{\"name\":\"edwinyxc@outlook.com is a fucker\",\"value\":\"Dog, Wung wung wung\"}"
        );
    }

    void default_json_serialization() throws InterruptedException, IOException, URISyntaxException {
        builder.clean().handler(new Router(
           app -> app.get("/*", ((CtxHandler<HttpCtx>) ctx -> {
               Context context = ctx.delegate();
               HttpCtx.Send http = () -> context;
               http.send(new Object(){
                   public String name = "name";
                   public String value = "value";
               });
           }))
        ));

        AssertEqualOnBody("http://localhost:9090/api/evil/a", "{\"name\":\"name\",\"value\":\"value\"}");
    }

    public void test_nested_router() throws IOException, URISyntaxException, InterruptedException {
        builder.clean().handler(new Router(
            app ->
                app.get("/api/*",
                    new Router()
                        .use("/evil/*",
                            new Router()
                                .get("/a",(req, resp) -> resp.send(200,"OK"))
                                .get("/",(req, resp) -> resp.send(200,"OK"))
                    )
                )
        ));


        AssertEqualOnBody( "http://localhost:9090/api/evil/a", "OK");

        AssertEqualOnBody("http://localhost:9090/api/evil/", "OK");
    }

    class ClassicRestfulRouter extends Router {
        {
            get("/:id/new", (req, resp) -> resp.send(req.param("id") + "/new1"));
            get("/new/:id", (req, resp) -> resp.send("new2/" + req.param("id")));
            get("/new", (req, resp) -> resp.send("new"));
            get("/:id", (req, resp) -> resp.send("id=" + req.param("id")));
        }
    }

    public void complex_route() throws InterruptedException, IOException, URISyntaxException {

        S.echo("Testing min_group_route");
        builder.clean().handler(new Router(
            p -> p.use("/*", new ClassicRestfulRouter())
        ));

        AssertEqualOnBody("http://localhost:9090/123/new", "123/new1");
        AssertEqualOnBody("http://localhost:9090/new/123", "new2/123");
        AssertEqualOnBody("http://localhost:9090/new", "new");
        AssertEqualOnBody("http://localhost:9090/233", "id=233");
    }

    public void basic_unicode() throws IOException, URISyntaxException, InterruptedException {
        S.echo("Testing utf8");
        builder.clean().handler(new Router(
            app ->
                app.get("/test", (req, resp) -> {
                    resp.write("中文支持");
                })
        ));
        AssertEqualOnBody("http://localhost:9090/test" ,"中文支持" );
    }

    public void basic_min_group_route() throws InterruptedException, IOException, URISyntaxException {

        S.echo("Testing min_group_route");
        builder.clean().handler(new Router(
            app -> {
                app.get("/:id/new", (req, resp) -> resp.send(req.param("id") + "/new1"));
                app.get("/new/:id", (req, resp) -> resp.send("new2/" + req.param("id")));
                app.get("/new", (req, resp) -> resp.send("new"));
                app.get("/:id", (req, resp) -> resp.send("id=" + req.param("id")));
            }
        ));

        AssertEqualOnBody("http://localhost:9090/123/new", "123/new1");
        AssertEqualOnBody("http://localhost:9090/new/123", "new2/123");
        AssertEqualOnBody("http://localhost:9090/new", "new");
        AssertEqualOnBody("http://localhost:9090/233", "id=233");
    }


//    public CompletableFuture<Void> assertEqualOnBody(String url, String judge) throws IOException, InterruptedException, URISyntaxException {
//        return CompletableFuture.runAsync(() -> {
//            try {
//                AssertEqualOnBody(url, judge);
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (URISyntaxException e) {
//                e.printStackTrace();
//            }
//        });
//    }


    public void basic_router() throws InterruptedException, IOException, URISyntaxException, ExecutionException {

        Router router = new Router();
        router.get("/add", (req, resp) -> resp.send("add"))
            .get("/del", (req, resp) -> resp.send("del"));

        builder.clean().handler(new Router(
            app ->
                app.get("/", (req, resp) -> resp.send("root"))
                    .get("/:id", (req, resp) -> resp.send(req.param("id")))
                    .get("/:id/text", (req, resp) -> resp.send("text"))
                    .use("/user/*", router)
        ));

        AssertEqualOnBody("http://localhost:9090/user/add", "add");
        AssertEqualOnBody("http://localhost:9090/user/del", "del");
        AssertEqualOnBody("http://localhost:9090/123", "123");
        AssertEqualOnBody("http://localhost:9090/123/text", "text");
//        AssertEqualOnBody("http://localhost:9090/123/add", "add");
    }

    void basic_ctx() throws URISyntaxException, IOException, InterruptedException {
        S.echo("Testing ctx_consistency");
        builder.clean().handler(new Router(
            app -> {
                app.use((req, resp) -> {
                    S.echo("INSTALLLLLLLLLLLLLLLLLLLLL");
                    req.ctx().put("val", 1);
                    var ctx = (RouterCtx)req.ctx()::bind;
                    ctx.continueRouting();
                });

                app.get("/testCtx", (req, resp) -> {
                    req.ctx().put("a", "a");
                    resp.send(200, req.ctx().get("a").toString() + req.ctx().get("val"));
                });
            }
        ));
        HttpRequest req =
            HttpRequest.newBuilder(new URI("http://localhost:9090/testCtx"))
                .GET()
                .build();

        var result = client.send(req, HttpResponse.BodyHandlers.ofString()).body();
        Assert.assertEquals("a1", result);
    }

    void basic() throws InterruptedException, IOException {
        builder.clean();
        builder.handler(
            new Router()
                .get("/", (req, res) -> {
                        HttpCtx ctx = req.ctx();
                        ctx.put("user", 1);
                    },
                    (req, res) -> {
                        HttpCtx ctx = req.ctx();
                        ctx.put("user", (int) ctx.get("user") + 1);
                    },
                    (req, res) -> {
                        HttpCtx ctx = req.ctx();
                        ctx.put("user", (int) ctx.get("user") + 1);
                    },
                    (req, res) -> {
                        Integer result = Convert.toInt(req.ctx().get("user"));
                        res.contentType("application/json;charset=utf8");
                        res.send(200, String.valueOf(result));
                    }
                )
        );
        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assert.assertEquals("3", response.body());

    }
    void test_cookie() throws IOException, InterruptedException {
        builder.clean();
        builder.handler(http -> {
            var ctx = (HttpCtx.Cookies) http::bind;
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
                var bind = (HttpCtx & HttpCtx.Send) ctx::bind;
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

    public static CtxHandler<HttpCtx> ok = ctx -> {
        //print all headers
        S.echo("headers", ((HttpCtx.Headers) ctx::bind).all());
        ((HttpCtx.Send) ctx::bind).Ok(HttpCtx.str("OK"));
    };

    public static CtxHandler<HttpCtx> sendFile = ctx -> {
        File file = new File(PATH.classpathRoot() + "logback.xml");
        //File file = new File("C:\\Users\\PC\\Downloads\\BaseItems_V1.zip");//big
        S.echo("Send File", file.getAbsolutePath());
        var send = (HttpCtx.Send) ctx::bind;
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
        var send = (HttpCtx.Send) ctx::bind;
        send.send(
            ctx.response(HttpResponseStatus.OK)
                .write("Hello").build()
        );
    };
}
