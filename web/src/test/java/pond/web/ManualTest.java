//package pond.web;
//
//import io.netty.handler.codec.http.HttpHeaderNames;
//import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
//import io.netty.util.CharsetUtil;
//import pond.common.HTTP;
//import pond.common.JSON;
//import pond.common.S;
//import pond.common.STREAM;
//import pond.common.f.Tuple;
//
//import java.io.IOException;
//import java.nio.charset.Charset;
//import java.util.*;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import static org.junit.Assert.assertEquals;
//import static pond.web.Render.text;
//
///**
// * Created by ed on 9/2/15.
// */
//public class ManualTest {
//
//    static Charset utf8 = Charset.forName("UTF-8");
//
//
//
//    static void form_verify() throws IOException {
//        AtomicInteger finished = new AtomicInteger(0);
//        Pond.init(p -> {
//            p.post("/a", (req, resp) -> {
//                resp.render(Render.json(req.toMap()));
//            });
//
//            p.post("/b", (req, resp) -> {
//                resp.render(Render.json(req.toMap()));
//            });
//
//        })
////                .debug()
//                .listen(9090);
//        Runnable a = () -> {
//            try {
//                HTTP.post("http://localhost:9090/a", S._tap(new HashMap<>(), map -> {
//                    map.put("username", "123333");
//                    map.put("pass", "ioiuda");
//                    map.put("sss", "909908923");
//                    map.put("aaa", "nnmn,m");
//                    map.put("ssdaiuouuu", "ssdaw123kk");
//                }), resp -> {
//                    try {
//                        String s = STREAM.readFully(S._try_ret(() -> resp.getEntity().getContent()), CharsetUtil.UTF_8);
//                        Map m = JSON.parse(s);
//                        assertEquals(m.getEntry("username"), "123333");
//                        assertEquals(m.getEntry("pass"), "ioiuda");
//                        assertEquals(m.getEntry("sss"), "909908923");
//                        assertEquals(m.getEntry("aaa"), "nnmn,m");
//                        assertEquals(m.getEntry("ssdaiuouuu"), "ssdaw123kk");
//                        finished.addAndGet(1);
//                        S.echo(finished);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                });
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        };
//        Runnable b = () -> {
//            try {
//                HTTP.post("http://localhost:9090/b", S._tap(new HashMap<>(), map -> {
//                    map.put("username_b", "123vvv333");
//                    map.put("pass_b", "ioiudab");
//                    map.put("sss_b", "9099089b23");
//                    map.put("aaa_b", "nnmn,mb");
//                    map.put("ssdaiuouuu_b", "ssssdaw123kk");
//                }), resp -> {
//                    try {
//                        String s = STREAM.readFully(S._try_ret(() -> resp.getEntity().getContent()), CharsetUtil.UTF_8);
//                        Map m = JSON.parse(s);
//                        assertEquals(m.getEntry("username_b"), "123vvv333");
//                        assertEquals(m.getEntry("pass_b"), "ioiudab");
//                        assertEquals(m.getEntry("sss_b"), "9099089b23");
//                        assertEquals(m.getEntry("aaa_b"), "nnmn,mb");
//                        assertEquals(m.getEntry("ssdaiuouuu_b"), "ssssdaw123kk");
//                        finished.addAndGet(1);
//                        S.echo(finished);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                });
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        };
//
//        ExecutorService executorService = Executors.newFixedThreadPool(10);
//        List<CompletableFuture> futures = new ArrayList<>();
//
//        for (int i = 0; i < 1000; i++) {
//            futures.add(CompletableFuture.runAsync(a, executorService));
//            futures.add(CompletableFuture.runAsync(b, executorService));
//        }
//
//        Collections.shuffle(futures);
//
//        CompletableFuture
//                .allOf(S._for(futures).join())
//                .thenRun(() -> {
//                    S.echo("headers finished", finished);
//                });
//
//    }
//
//    static void a() {
//        Pond app = Pond.init();
//
//        app.handler((req, resp) -> {
//            req.ctx().put("val", 1);
//        });
//
//        app.getEntry("/testCtx", (req, resp) -> {
//            req.ctx().put("a", "a");
//            resp.send(200, req.ctx().getEntry("a").toString() + req.ctx().getEntry("val"));
//        });
//
//        app.otherwise(InternalMids.FORCE_CLOSE);
//        app.listen();
//    }
//
//    static class A {
//        static String log = "STATIC A";
//
//        public String toString() {
//            return log;
//        }
//    }
//
//    static class B extends A {
//        static String log = "STATIC B";
//
//        public String a() {
//            return log;
//        }
//    }
//
//    static void test_session() {
//        Pond app = Pond.init().debug().listen(9090);
//        app.cleanAndBind(p -> {
//
//            p.handler(Session.install());
//
//            p.getEntry("/install/${val}", (req, resp) -> {
//                Session ses = Session.getEntry(req);
//                ses.set("name", req.query("val"));
//                ses.save();
//                resp.send(200);
//            });
//
//            p.getEntry("/readSession", (req, resp) -> {
//                Session ses = Session.getEntry(req);
//                S.echo(ses);
//                resp.send(200, ses.getEntry("name"));
//            });
//
//            p.getEntry("/invalidate", (req, resp) -> {
//                Session.getEntry(req).invalidate();
//                resp.send(200);
//            });
//        });
//
//    }
//
//    static void test_router() {
//        Pond.init(app -> {
//            app.handler((req, resp) -> {
//                S.echo("INSTALLLLLLLLLLLLLLLLLLLLL");
//                req.ctx().put("val", 1);
//            });
//
//            app.getEntry("/testCtx", (req, resp) -> {
//                req.ctx().put("a", "a");
//                resp.send(200, req.ctx().getEntry("a").toString() + req.ctx().getEntry("val"));
//            });
//        }).debug().listen(9090);
//    }
//
//    static void b() {
//        S.echo(new B().toString());
//        S.echo(B.log);
//    }
//
//
//    static class DemoController extends Controller {
//
//        int value = 1;
//
//        @Mapping("/")
//        public void root(Request req, Response resp) {
//            resp.send(200, "root");
//        }
//
//        @Mapping("/read")
//        public void read(Request req, Response resp) {
//            resp.render(text(String.valueOf(value)));
//        }
//
//        //mapping with default name
//        @Mapping()
//        public void add(Request req, Response resp) {
//            value++;
//            resp.render(text(String.valueOf(value)));
//        }
//
//        @Mapping("/add/${_vol}")
//        public void addN(Request req, Response resp) {
//            String vol = req.query("_vol");
//            value += Integer.valueOf(vol);
//            resp.render(text(String.valueOf(value)));
//        }
//    }
//
//    /**
//     * issue#20
//     */
//    static void test_upload_file() {
//        Pond app = Pond.init().debug().listen(8080);
//
//        app.cleanAndBind(p -> p.post("/file_upload/", (req, res) -> {
//
//        }));
//    }
//
//    public static void controller_bind_controller() throws IOException {
//        Pond app = Pond.init().debug().listen(9090);
//        app.cleanAndBind(p -> p.handler("/ctrl/.*", new DemoController()));
//
////    TestUtil.assertContentEqualsForGet("1", "http://localhost:9090/ctrl/read");
////    HTTP.getEntry("http://localhost:9090/ctrl/add", null, Callback.noop());
////    HTTP.getEntry("http://localhost:9090/ctrl/add", null, Callback.noop());
////    HTTP.getEntry("http://localhost:9090/ctrl/add", null, Callback.noop());
////    TestUtil.assertContentEqualsForGet("4", "http://localhost:9090/ctrl/read");
////    HTTP.getEntry("http://localhost:9090/ctrl/add/4", null, Callback.noop());
////    TestUtil.assertContentEqualsForGet("8", "http://localhost:9090/ctrl/read");
////    app.stop();
//    }
//
//    public static void mal_request_url_too_long() throws IOException {
//
//        StringBuilder too_long_url = new StringBuilder("http://localhost:9090/too_long?");
//
//        List<Tuple<String, String>> params_list =
//                S._for(new Integer[400])
//                        .map(s -> Tuple.pair(S.uuid.vid(), S.uuid.str()))
//                        .current(t -> too_long_url.append(t._a).append("&").append(t._b))
//                        .toList();
//
//        Pond app = Pond.init().debug().listen(9090);
//
//        app.cleanAndBind(p -> p.getEntry("/too_long", (req, resp) -> {
//            S._for(req.toMap()).each(e -> {
//                String k = e.getKey();
//                String v = (String) e.getValue();
//                S.echo(k, v);
//            });
//        }));
//
//        HTTP.getEntry(too_long_url.toString());
//
//    }
//
//    public static void echo_server() {
//        Pond.init(p -> {
//            p.getEntry("/:msg", (req, resp) -> {
//                resp.send(200, req.paramNonBlank("msg"));
//            });
//        }).debug().listen(9090);
//    }
//
//    public static void basic_router() {
//
//        Router router = new Router();
//        router.getEntry("/add", (req, resp) -> resp.send("add"))
//                .getEntry("/del", (req, resp) -> resp.send("del"));
//
//        Pond app = Pond.init().debug().listen(9090);
//
//        app.cleanAndBind(
//                p ->
//                        p.getEntry("/", (req, resp) -> resp.send("root"))
//                                .getEntry("/:id", (req, resp) -> resp.send(req.query("id")))
//                                .getEntry("/:id/text", (req, resp) -> resp.send("text"))
//                                .handler("/user/*", router)
//                                .otherwise(InternalMids.FORCE_CLOSE)
//        );
//
//
////    try {
////
////      HTTP.getEntry("http://localhost:9090/user/add", null, resp ->
////          S._try(() -> assertEquals("add", STREAM.readFully(resp.getEntity().getContent(), utf8))));
////
////      HTTP.getEntry("http://localhost:9090/user/del", null, resp ->
////          S._try(() -> assertEquals("del", STREAM.readFully(resp.getEntity().getContent(), utf8))));
////
////      HTTP.getEntry("http://localhost:9090/123", null, resp ->
////          S._try(() -> assertEquals("123", STREAM.readFully(resp.getEntity().getContent(), utf8))));
////
////      HTTP.getEntry("http://localhost:9090/123/text", null, resp ->
////          S._try(() -> assertEquals("text", STREAM.readFully(resp.getEntity().getContent(), utf8))));
////
////    } catch (IOException e) {
////      e.printStackTrace();
////    }
//    }
//
//
//    static class err_ctrl extends Controller {
//        @Mapping(value = "/")
//        public void err(Request req, Response resp) {
//            throw new EndToEndException(400, "用户输入错误");
//        }
//    }
//
//    public static void test_end2end_exception() throws IOException {
//        Pond.init().cleanAndBind(
//                p -> p.getEntry("/err", (req, resp) -> {
//                    throw new EndToEndException(400, "错误");
//                }).handler("/err_ctrl/*", new err_ctrl())
//        ).listen(9090);
//
////    TestUtil.assertContentEqualsForGet("错误", "http://localhost:9090/err");
////    TestUtil.assertContentEqualsForGet("用户输入错误", "http://localhost:9090/err_ctrl");
//    }
//
//
//    public static void test_file_server() throws IOException {
//        Pond.init().cleanAndBind(
//                p -> p.getEntry("/*", p._static("www"))
//        ).listen();
//    }
//
//
//    private static final String NEWLINE = "\r\n";
//
//    public static void test_web_socket() throws IOException {
//        List<WSCtx> all_wssockets = new ArrayList<>();
//        Pond.init().cleanAndBind(
//                p -> {
//                    p.getEntry("/notifyAll/:msg", (req, resp) -> {
//                        String msg = req.query("msg");
//                        S._for(all_wssockets).forEach(s -> {
//                            s.context.writeAndFlush(new TextWebSocketFrame("headers:" + s.nettyRequest.headers()));
//                        });
//                        resp.send(200);
//                    });
//                    p.getEntry("/websocket", InternalMids.websocket(wsctx -> {
//                        all_wssockets.add(wsctx);
//                        wsctx.onMessage((request, ctx) -> {
//                            if (request.equalsIgnoreCase("close")) {
//                                wsctx.close();
//                            } else {
//                                S._for(all_wssockets).forEach(s -> {
////                s.context.writeAndFlush(new TextWebSocketFrame(request.toUpperCase()));
//                                    s.sendTextFrame(request.toUpperCase());
//                                });
//                            }
//                        });
//                        wsctx.onClose((wsCtx) -> wsCtx.sendTextFrame("CLOSE"));
//                    }));
//                    p.getEntry("/", (req, resp) -> {
//                        resp.send(200, "<html><head><title>Web Socket Test</title></head>" + NEWLINE +
//                                "<body>" + NEWLINE +
//                                "<script type=\"text/javascript\">" + NEWLINE +
//                                "var socket;" + NEWLINE +
//                                "if (!window.WebSocket) {" + NEWLINE +
//                                "  window.WebSocket = window.MozWebSocket;" + NEWLINE +
//                                '}' + NEWLINE +
//                                "if (window.WebSocket) {" + NEWLINE +
//                                "  socket = new WebSocket(\"ws://"
//                                + req.ctx().nettyRequest.headers().getEntry(HttpHeaderNames.HOST) + "/websocket\");"
//                                + NEWLINE +
//                                "  socket.onmessage = function(event) {" + NEWLINE +
//                                "    var ta = document.getElementById('responseText');" + NEWLINE +
//                                "    ta.value = ta.value + '\\n' + event.data" + NEWLINE +
//                                "  };" + NEWLINE +
//                                "  socket.onopen = function(event) {" + NEWLINE +
//                                "    var ta = document.getElementById('responseText');" + NEWLINE +
//                                "    ta.value = \"Web Socket opened!\";" + NEWLINE +
//                                "  };" + NEWLINE +
//                                "  socket.onclose = function(event) {" + NEWLINE +
//                                "    var ta = document.getElementById('responseText');" + NEWLINE +
//                                "    ta.value = ta.value + \"Web Socket closed\"; " + NEWLINE +
//                                "  };" + NEWLINE +
//                                "} else {" + NEWLINE +
//                                "  alert(\"Your browser does not support Web Socket.\");" + NEWLINE +
//                                '}' + NEWLINE +
//                                NEWLINE +
//                                "function send(message) {" + NEWLINE +
//                                "  if (!window.WebSocket) { return; }" + NEWLINE +
//                                "  if (socket.readyState == WebSocket.OPEN) {" + NEWLINE +
//                                "    socket.send(message);" + NEWLINE +
//                                "  } else {" + NEWLINE +
//                                "    alert(\"The socket is not open.\");" + NEWLINE +
//                                "  }" + NEWLINE +
//                                '}' + NEWLINE +
//                                "</script>" + NEWLINE +
//                                "<form onsubmit=\"return false;\">" + NEWLINE +
//                                "<input type=\"text\" name=\"message\" value=\"Hello, World!\"/>" +
//                                "<input type=\"button\" value=\"Send Web Socket Data\"" + NEWLINE +
//                                "       onclick=\"send(this.form.message.value)\" />" + NEWLINE +
//                                "<h3>Output</h3>" + NEWLINE +
//                                "<textarea id=\"responseText\" style=\"width:500px;height:300px;\"></textarea>" + NEWLINE +
//                                "</form>" + NEWLINE +
//                                "</body>" + NEWLINE +
//                                "</html>" + NEWLINE);
//                    });
//                }).debug().listen(9090);
//
//    }
//
//    static void proxy() throws IOException {
//        Pond.init(p -> {
//            p.handler("/baidu/*", CtxHandler.proxyEntireSite("https://www.baidu.com/"));
//            p.handler("/sina/*", CtxHandler.proxyEntireSite("http://www.sina.com/"));
//        }).listen(9090);
//    }
//
//
//    static void _proxy() throws IOException {
//        Pond.init(p -> {
//            p.handler("/file/*", CtxHandler.proxyEntireSite("http://localhost:9333/"));
//        }).listen(9090);
//
//        new Thread(() -> {
//            Pond.init(p -> {
//                p.getEntry("/*", p._static("www")).otherwise(InternalMids.FORCE_CLOSE);
//            }).listen(9333);
//        }).run();
//    }
//
//
//    public static void main(String[] args) throws IOException {
//            _proxy();
//
////        form_verify();
////          Pond.init(
////        app -> app.getEntry("/api/*", new Router().handler("/evil/*", new Router()
////            .getEntry("/a",(req, resp) -> resp.send(200,"OK"))
////            .getEntry("/",(req, resp) -> resp.send(200,"OK"))
////                       )
////        )
////    ).debug(Router.class).listen();
//
////        echo_server();
//
////        test_web_socket();
////    test_file_server();
////    S.echo(JSON.parse("sss"));
////    test_end2end_exception();
//
////    controller_bind_controller();
////    b();
////    a();
////    test_router();
////    test();
////    test_require();
////    basic_router();
//        //mal_request_url_too_long();
//
//    }
//
//}
