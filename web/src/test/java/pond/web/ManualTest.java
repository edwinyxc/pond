package pond.web;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.CharsetUtil;
import pond.common.HTTP;
import pond.common.JSON;
import pond.common.S;
import pond.common.STREAM;
import pond.common.f.Tuple;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static pond.web.Render.text;

/**
 * Created by ed on 9/2/15.
 */
public class ManualTest {

    static Charset utf8 = Charset.forName("UTF-8");

    static void form_verify() throws IOException {
        AtomicInteger finished = new AtomicInteger(0);
        Pond.init(p -> {
            p.post("/a", (req, resp) -> {
                resp.render(Render.json(req.toMap()));
            });

            p.post("/b", (req, resp) -> {
                resp.render(Render.json(req.toMap()));
            });

        })
//                .debug()
                .listen(9090);
        Runnable a = () -> {
            try {
                HTTP.post("http://localhost:9090/a", S._tap(new HashMap<>(), map -> {
                    map.put("username", "123333");
                    map.put("pass", "ioiuda");
                    map.put("sss", "909908923");
                    map.put("aaa", "nnmn,m");
                    map.put("ssdaiuouuu", "ssdaw123kk");
                }), resp -> {
                    try {
                        String s = STREAM.readFully(S._try_ret(() -> resp.getEntity().getContent()), CharsetUtil.UTF_8);
                        Map m = JSON.parse(s);
                        assertEquals(m.get("username"), "123333");
                        assertEquals(m.get("pass"), "ioiuda");
                        assertEquals(m.get("sss"), "909908923");
                        assertEquals(m.get("aaa"), "nnmn,m");
                        assertEquals(m.get("ssdaiuouuu"), "ssdaw123kk");
                        finished.addAndGet(1);
                        S.echo(finished);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        Runnable b = () -> {
            try {
                HTTP.post("http://localhost:9090/b", S._tap(new HashMap<>(), map -> {
                    map.put("username_b", "123vvv333");
                    map.put("pass_b", "ioiudab");
                    map.put("sss_b", "9099089b23");
                    map.put("aaa_b", "nnmn,mb");
                    map.put("ssdaiuouuu_b", "ssssdaw123kk");
                }), resp -> {
                    try {
                        String s = STREAM.readFully(S._try_ret(() -> resp.getEntity().getContent()), CharsetUtil.UTF_8);
                        Map m = JSON.parse(s);
                        assertEquals(m.get("username_b"), "123vvv333");
                        assertEquals(m.get("pass_b"), "ioiudab");
                        assertEquals(m.get("sss_b"), "9099089b23");
                        assertEquals(m.get("aaa_b"), "nnmn,mb");
                        assertEquals(m.get("ssdaiuouuu_b"), "ssssdaw123kk");
                        finished.addAndGet(1);
                        S.echo(finished);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<CompletableFuture> futures = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            futures.add(CompletableFuture.runAsync(a, executorService));
            futures.add(CompletableFuture.runAsync(b, executorService));
        }

        Collections.shuffle(futures);

        CompletableFuture
                .allOf(S._for(futures).join())
                .thenRun(() -> {
                    S.echo("all finished", finished);
                });

    }

    static void a() {
        Pond app = Pond.init();

        app.use((req, resp) -> {
            req.ctx().put("val", 1);
        });

        app.get("/testCtx", (req, resp) -> {
            req.ctx().put("a", "a");
            resp.send(200, req.ctx().get("a").toString() + req.ctx().get("val"));
        });

        app.otherwise(InternalMids.FORCE_CLOSE);
        app.listen();
    }

    static class A {
        static String log = "STATIC A";

        public String toString() {
            return log;
        }
    }

    static class B extends A {
        static String log = "STATIC B";

        public String a() {
            return log;
        }
    }

    static void test_session() {
        Pond app = Pond.init().debug().listen(9090);
        app.cleanAndBind(p -> {

            p.use(Session.install());

            p.get("/install/${val}", (req, resp) -> {
                Session ses = Session.get(req);
                ses.set("name", req.param("val"));
                ses.save();
                resp.send(200);
            });

            p.get("/readSession", (req, resp) -> {
                Session ses = Session.get(req);
                S.echo(ses);
                resp.send(200, ses.get("name"));
            });

            p.get("/invalidate", (req, resp) -> {
                Session.get(req).invalidate();
                resp.send(200);
            });
        });

    }

    static void test_router() {
        Pond.init(app -> {
            app.use((req, resp) -> {
                S.echo("INSTALLLLLLLLLLLLLLLLLLLLL");
                req.ctx().put("val", 1);
            });

            app.get("/testCtx", (req, resp) -> {
                req.ctx().put("a", "a");
                resp.send(200, req.ctx().get("a").toString() + req.ctx().get("val"));
            });
        }).debug().listen(9090);
    }

    static void b() {
        S.echo(new B().toString());
        S.echo(B.log);
    }


    static class DemoController extends Controller {

        int value = 1;

        @Mapping("/")
        public void root(Request req, Response resp) {
            resp.send(200, "root");
        }

        @Mapping("/read")
        public void read(Request req, Response resp) {
            resp.render(text(String.valueOf(value)));
        }

        //mapping with default name
        @Mapping()
        public void add(Request req, Response resp) {
            value++;
            resp.render(text(String.valueOf(value)));
        }

        @Mapping("/add/${_vol}")
        public void addN(Request req, Response resp) {
            String vol = req.param("_vol");
            value += Integer.valueOf(vol);
            resp.render(text(String.valueOf(value)));
        }
    }

    /**
     * issue#20
     */
    static void test_upload_file() {
        Pond app = Pond.init().debug().listen(8080);

        app.cleanAndBind(p -> p.post("/file_upload/", (req, res) -> {

        }));
    }

    public static void controller_bind_controller() throws IOException {
        Pond app = Pond.init().debug().listen(9090);
        app.cleanAndBind(p -> p.use("/ctrl/.*", new DemoController()));

//    TestUtil.assertContentEqualsForGet("1", "http://localhost:9090/ctrl/read");
//    HTTP.get("http://localhost:9090/ctrl/add", null, Callback.noop());
//    HTTP.get("http://localhost:9090/ctrl/add", null, Callback.noop());
//    HTTP.get("http://localhost:9090/ctrl/add", null, Callback.noop());
//    TestUtil.assertContentEqualsForGet("4", "http://localhost:9090/ctrl/read");
//    HTTP.get("http://localhost:9090/ctrl/add/4", null, Callback.noop());
//    TestUtil.assertContentEqualsForGet("8", "http://localhost:9090/ctrl/read");
//    app.stop();
    }

    public static void mal_request_url_too_long() throws IOException {

        StringBuilder too_long_url = new StringBuilder("http://localhost:9090/too_long?");

        List<Tuple<String, String>> params_list =
                S._for(new Integer[400])
                        .map(s -> Tuple.pair(S.uuid.vid(), S.uuid.str()))
                        .peek(t -> too_long_url.append(t._a).append("&").append(t._b))
                        .toList();

        Pond app = Pond.init().debug().listen(9090);

        app.cleanAndBind(p -> p.get("/too_long", (req, resp) -> {
            S._for(req.toMap()).each(e -> {
                String k = e.getKey();
                String v = (String) e.getValue();
                S.echo(k, v);
            });
        }));

        HTTP.get(too_long_url.toString());

    }

    public static void echo_server() {
        Pond.init(p -> {
            p.get("/:msg", (req, resp) -> {
                resp.send(200, req.paramNonBlank("msg"));
            });
        }).debug().listen(9090);
    }

    public static void basic_router() {

        Router router = new Router();
        router.get("/add", (req, resp) -> resp.send("add"))
                .get("/del", (req, resp) -> resp.send("del"));

        Pond app = Pond.init().debug().listen(9090);

        app.cleanAndBind(
                p ->
                        p.get("/", (req, resp) -> resp.send("root"))
                                .get("/:id", (req, resp) -> resp.send(req.param("id")))
                                .get("/:id/text", (req, resp) -> resp.send("text"))
                                .use("/user/*", router)
                                .otherwise(InternalMids.FORCE_CLOSE)
        );


//    try {
//
//      HTTP.get("http://localhost:9090/user/add", null, resp ->
//          S._try(() -> assertEquals("add", STREAM.readFully(resp.getEntity().getContent(), utf8))));
//
//      HTTP.get("http://localhost:9090/user/del", null, resp ->
//          S._try(() -> assertEquals("del", STREAM.readFully(resp.getEntity().getContent(), utf8))));
//
//      HTTP.get("http://localhost:9090/123", null, resp ->
//          S._try(() -> assertEquals("123", STREAM.readFully(resp.getEntity().getContent(), utf8))));
//
//      HTTP.get("http://localhost:9090/123/text", null, resp ->
//          S._try(() -> assertEquals("text", STREAM.readFully(resp.getEntity().getContent(), utf8))));
//
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
    }


    static class err_ctrl extends Controller {
        @Mapping(value = "/")
        public void err(Request req, Response resp) {
            throw new EndToEndException(400, "用户输入错误");
        }
    }

    public static void test_end2end_exception() throws IOException {
        Pond.init().cleanAndBind(
                p -> p.get("/err", (req, resp) -> {
                    throw new EndToEndException(400, "错误");
                }).use("/err_ctrl/*", new err_ctrl())
        ).listen(9090);

//    TestUtil.assertContentEqualsForGet("错误", "http://localhost:9090/err");
//    TestUtil.assertContentEqualsForGet("用户输入错误", "http://localhost:9090/err_ctrl");
    }


    public static void test_file_server() throws IOException {
        Pond.init().cleanAndBind(
                p -> p.get("/*", p._static("www"))
        ).listen();
    }


    private static final String NEWLINE = "\r\n";

    public static void test_web_socket() throws IOException {
        List<WSCtx> all_wssockets = new ArrayList<>();
        Pond.init().cleanAndBind(
                p -> {
                    p.get("/notifyAll/:msg", (req, resp) -> {
                        String msg = req.param("msg");
                        S._for(all_wssockets).forEach(s -> {
                            s.context.writeAndFlush(new TextWebSocketFrame("headers:" + s.nettyRequest.headers()));
                        });
                        resp.send(200);
                    });
                    p.get("/websocket", InternalMids.websocket(wsctx -> {
                        all_wssockets.add(wsctx);
                        wsctx.onMessage((request, ctx) -> {
                            if (request.equalsIgnoreCase("close")) {
                                wsctx.close();
                            } else {
                                S._for(all_wssockets).forEach(s -> {
//                s.context.writeAndFlush(new TextWebSocketFrame(request.toUpperCase()));
                                    s.sendTextFrame(request.toUpperCase());
                                });
                            }
                        });
                        wsctx.onClose((wsCtx) -> wsCtx.sendTextFrame("CLOSE"));
                    }));
                    p.get("/", (req, resp) -> {
                        resp.send(200, "<html><head><title>Web Socket Test</title></head>" + NEWLINE +
                                "<body>" + NEWLINE +
                                "<script type=\"text/javascript\">" + NEWLINE +
                                "var socket;" + NEWLINE +
                                "if (!window.WebSocket) {" + NEWLINE +
                                "  window.WebSocket = window.MozWebSocket;" + NEWLINE +
                                '}' + NEWLINE +
                                "if (window.WebSocket) {" + NEWLINE +
                                "  socket = new WebSocket(\"ws://"
                                + req.ctx().nettyRequest.headers().get(HttpHeaderNames.HOST) + "/websocket\");"
                                + NEWLINE +
                                "  socket.onmessage = function(event) {" + NEWLINE +
                                "    var ta = document.getElementById('responseText');" + NEWLINE +
                                "    ta.value = ta.value + '\\n' + event.data" + NEWLINE +
                                "  };" + NEWLINE +
                                "  socket.onopen = function(event) {" + NEWLINE +
                                "    var ta = document.getElementById('responseText');" + NEWLINE +
                                "    ta.value = \"Web Socket opened!\";" + NEWLINE +
                                "  };" + NEWLINE +
                                "  socket.onclose = function(event) {" + NEWLINE +
                                "    var ta = document.getElementById('responseText');" + NEWLINE +
                                "    ta.value = ta.value + \"Web Socket closed\"; " + NEWLINE +
                                "  };" + NEWLINE +
                                "} else {" + NEWLINE +
                                "  alert(\"Your browser does not support Web Socket.\");" + NEWLINE +
                                '}' + NEWLINE +
                                NEWLINE +
                                "function send(message) {" + NEWLINE +
                                "  if (!window.WebSocket) { return; }" + NEWLINE +
                                "  if (socket.readyState == WebSocket.OPEN) {" + NEWLINE +
                                "    socket.send(message);" + NEWLINE +
                                "  } else {" + NEWLINE +
                                "    alert(\"The socket is not open.\");" + NEWLINE +
                                "  }" + NEWLINE +
                                '}' + NEWLINE +
                                "</script>" + NEWLINE +
                                "<form onsubmit=\"return false;\">" + NEWLINE +
                                "<input type=\"text\" name=\"message\" value=\"Hello, World!\"/>" +
                                "<input type=\"button\" value=\"Send Web Socket Data\"" + NEWLINE +
                                "       onclick=\"send(this.form.message.value)\" />" + NEWLINE +
                                "<h3>Output</h3>" + NEWLINE +
                                "<textarea id=\"responseText\" style=\"width:500px;height:300px;\"></textarea>" + NEWLINE +
                                "</form>" + NEWLINE +
                                "</body>" + NEWLINE +
                                "</html>" + NEWLINE);
                    });
                }).debug().listen(9090);

    }

    public static void main(String[] args) throws IOException {

//        form_verify();
//          Pond.init(
//        app -> app.get("/api/*", new Router().use("/evil/*", new Router()
//            .get("/a",(req, resp) -> resp.send(200,"OK"))
//            .get("/",(req, resp) -> resp.send(200,"OK"))
//                       )
//        )
//    ).debug(Router.class).listen();

        echo_server();

//        test_web_socket();
//    test_file_server();
//    S.echo(JSON.parse("sss"));
//    test_end2end_exception();

//    controller_bind_controller();
//    b();
//    a();
//    test_router();
//    test();
//    test_require();
//    basic_router();
        //mal_request_url_too_long();

    }

}
