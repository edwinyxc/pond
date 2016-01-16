package pond.web;

import io.netty.handler.codec.http.ClientCookieDecoder;
import io.netty.handler.codec.http.ClientCookieEncoder;
import io.netty.handler.codec.http.Cookie;
import io.netty.util.CharsetUtil;
import org.apache.http.Header;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pond.common.*;
import pond.common.f.Callback;
import pond.common.f.Holder;
import pond.web.spi.BaseServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static pond.web.Render.text;

/**
 * Created by ed on 8/22/15.
 */
public class TestSuite {

  Pond app;

  Charset utf8 = Charset.forName("UTF-8");


  @Before
  public void init() {
    app = Pond.init().debug();
    S.config.set(BaseServer.class, BaseServer.PORT, "9090");
//    System.setProperty("file.encoding","utf8");
    app.listen();
  }

  @Test
  public void test() throws IOException {
    //BASIC
    basic();
    basic_ctx();
    basic_router();
    basic_min_group_route();
    basic_unicode();

    //STATIC
    static_bind_non_root();
    static_bind_root();
    static_default_index();

    //RENDER
    render_json();
    render_text();

    //MULTIPART
    multipart();

    //CONTROLLER
    controller_bind_controller();
    controller_bind_controller_to_root();

    //SESSION
    session_test();
    session_custom_test();

    //FORM-VERIFY
    form_verify();

    //user-custom
    test_end2end_exception();
  }

  public void form_verify() throws IOException {
    app.cleanAndBind(p -> {
      p.post("/a", (req, resp) -> {
        resp.render(Render.json(req.toMap()));
      });

      p.post("/b", (req, resp) -> {
        resp.render(Render.json(req.toMap()));
      });

    });

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

    try {
      CompletableFuture.allOf(S._for(futures).join()).get();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
  }

//  public void require_test() throws IOException {
//    Mid session = Session.install();
//    app.cleanAndBind(p -> {
//
//      p.get("/require",
//            session,
//            Mid.wrap((req, resp) -> resp.send(200, "pass")).require(session)
//      );
//
//      p.get("/requireFail",
//            Mid.wrap((req, resp) -> resp.send(200, "pass")).require(session)
//      );
//
//    });
//
//    TestUtil.assertContentEqualsForGet("pass", "http://localhost:9090/require");
//    HTTP.get("http://localhost:9090/requireFail", http -> {
//      assertNotSame(200, http.getStatusLine().getStatusCode());
//    });
//
//  }

  public void session_custom_test() throws IOException {

    Mid session = Session.install(req -> req.header("sessionid-in-header"),
                                  (req, resp) -> resp.send(403, "require session"));

    app.cleanAndBind(
        app -> {
          app.get("/test", session, (req, resp) -> resp.send(200, Session.get(req).id));
          app.get("/login", (req, resp) -> resp.send(200, Session.store().create(new HashMap<>())));
        }
    );

    HTTP.get("http://localhost:9090/login", resp -> {
      try {
        String sid = STREAM.readFully(S._try_ret(() -> resp.getEntity().getContent()), Charset.defaultCharset());

        HTTP.get("http://localhost:9090/test", S._tap(new HashMap<>(), m -> {
          m.put("sessionid-in-header", sid);
        }), response -> {
          try {
            String ret = STREAM.readFully(response.getEntity().getContent(), Charset.defaultCharset());
            assertEquals(sid, ret);
          } catch (IOException e) {
            e.printStackTrace();
          }

        });

      } catch (IOException e) {
        e.printStackTrace();
      }
    });


  }

  public void session_test() throws IOException {
    app.cleanAndBind(
        app -> {
          app.use(Session.install());

          app.get("/installSession", (req, resp) -> {
            Session ses = Session.get(req);
            ses.set("name", "user1");
            ses.save();
            resp.send(200);
          });

          app.get("/readSession", (req, resp) -> {
            Session ses = Session.get(req);
            resp.send(200, ses.get("name"));
          });

          app.get("/invalidate", (req, resp) -> {
            Session.get(req).invalidate();
            resp.send(200);
          });
        }
    );

    Holder<String> sessionHolder = new Holder<>();

    HTTP.get("http://localhost:9090/installSession", http -> {
      Header cookieHeader = http.getFirstHeader("Set-Cookie");
      Cookie cookie = ClientCookieDecoder.decode(cookieHeader.getValue());
      sessionHolder.val(cookie.value());
    });

    //read session
    HTTP.get("http://localhost:9090/readSession",
             S._tap(new HashMap<>(), h -> h.put("Cookie", ClientCookieEncoder.encode(Session.LABEL_SESSION, sessionHolder.val()))),
             resp -> {
               String result = S._try_ret(
                   () -> STREAM.readFully(S._try_ret(
                       () -> resp.getEntity().getContent()), CharsetUtil.UTF_8));
               assertEquals("user1", result);
             }
    );

    HTTP.get("http://localhost:9090/invalidate",
             S._tap(new HashMap<>(), h -> h.put("Cookie", ClientCookieEncoder.encode(Session.LABEL_SESSION, sessionHolder.val())))
    );

    //read session
    HTTP.get("http://localhost:9090/readSession",
             S._tap(new HashMap<>(), h -> h.put("Cookie", ClientCookieEncoder.encode(Session.LABEL_SESSION, sessionHolder.val()))),
             resp -> {
               String result = S._try_ret(
                   () -> STREAM.readFully(S._try_ret(
                       () -> resp.getEntity().getContent()), CharsetUtil.UTF_8));
               assertEquals("null", result);
             }
    );

  }

  class DemoController extends Controller {

    AtomicInteger value = new AtomicInteger(1);

    @Mapping("/")
    public void root(Request req, Response resp) {
      resp.send(200, "root");
    }

    @Mapping("/read")
    public void read(Request req, Response resp) {
      resp.render(text(String.valueOf(value.get())));
    }

    //mapping with default name
    @Mapping
    public void add(Request req, Response resp) {
      value.getAndAdd(1);
      resp.render(text(String.valueOf(value)));
    }

    @Mapping("/add/:_vol")
    public void addN(Request req, Response resp) {
      String vol = req.param("_vol");
      value.getAndAdd(Integer.valueOf(vol));
      resp.render(text(String.valueOf(value.get())));
    }

    @Mapping
    public void ctx(Request req, Response resp) {
      resp.render(text(String.valueOf(req.ctx().get("k"))));
    }

  }

  public void controller_bind_controller() throws IOException {

    app.cleanAndBind(app -> {
      app.use("/ctrl/*",
              new DemoController());
      app.use("/ctx/*",
              (req, resp) -> req.ctx().put("k", "v"),
              new DemoController());
    });

    TestUtil.assertContentEqualsForGet("1", "http://localhost:9090/ctrl/read");
    TestUtil.assertContentEqualsForGet("root", "http://localhost:9090/ctrl/");
    HTTP.get("http://localhost:9090/ctrl/add", Callback.noop());
    HTTP.get("http://localhost:9090/ctrl/add", Callback.noop());
    HTTP.get("http://localhost:9090/ctrl/add", Callback.noop());
    TestUtil.assertContentEqualsForGet("4", "http://localhost:9090/ctrl/read");
    HTTP.get("http://localhost:9090/ctrl/add/4", Callback.noop());

    TestUtil.assertContentEqualsForGet("8", "http://localhost:9090/ctrl/read");
    TestUtil.assertContentEqualsForGet("v", "http://localhost:9090/ctx/ctx");

  }

  public void controller_bind_controller_to_root() throws IOException {

    S._debug_on(Route.class);
    app.cleanAndBind(
        p -> p.use("/*", new DemoController())
    );

    TestUtil.assertContentEqualsForGet("1", "http://localhost:9090/read");

  }

  public void multipart() throws IOException {

    app.cleanAndBind(
        app ->
            app.post("/multipart", (req, resp) -> {
              Request.UploadFile f = req.file("content");
              try {
                STREAM.pipe(f.inputStream(), resp.out());
              } catch (IOException e) {
                e.printStackTrace();
              }

              resp.send(200);
            })
    );


    File wwwroot = new File(S.config.get(Pond.class, Pond.CONFIG_WEB_ROOT), "www");

    HTTP.postMultipart(
        "http://localhost:9090/multipart",

        S._tap(new HashMap<>(), map -> {
          map.put("text1", "text1");
          map.put("text2", "text2");
        }),

        S._tap(new HashMap<>(), map -> {
          map.put("content", S._try_ret(() -> new File(wwwroot, "test_lv.jpg")));
        }),

        null,

        resp -> {
          try (InputStream in = resp.getEntity().getContent();
               FileInputStream file_in = new FileInputStream(new File(wwwroot, "test_lv.jpg"))
          ) {
            byte[] data = STREAM.readFully(in);
            byte[] file = STREAM.readFully(file_in);

            assertArrayEquals(file, data);

          } catch (IOException e) {
            e.printStackTrace();
          }

        }
    );

  }

  public void render_json() throws IOException {

    String json = "{\"a\":\"a\",\"b\":\"b\"}";
    Map jsonMap = JSON.parse(json);
    app.cleanAndBind(
        app ->
            app.get("/test_render_json", (req, resp) ->
                resp.render(Render.json(jsonMap)))
    );

    TestUtil.assertContentEqualsForGet(json, "http://localhost:9090/test_render_json");
  }

  public void render_text() throws IOException {
    String text = "sdddaaa";
    app.cleanAndBind(
        app ->
            app.get("/test_render_text", (req, resp) ->
                resp.render(text(text)))
    );


    TestUtil.assertContentEqualsForGet(text, "http://localhost:9090/test_render_text");
  }


  public void static_bind_root() throws IOException {
    app.cleanAndBind(app -> app.get("/*", app._static("www")));

    TestUtil.assertContentEqualsForGet(
        "app.js", "http://localhost:9090/123.html"
    );
  }

  public void static_bind_non_root() throws IOException {
    app.cleanAndBind(app -> app.get("/static/*", app._static("www")));

    TestUtil.assertContentEqualsForGet(
        "app.js", "http://localhost:9090/static/123.html"
    );
  }

  public void static_default_index() throws IOException {
    app.cleanAndBind(app -> app.get("/*", app._static("www")));

    TestUtil.assertContentEqualsForGet(
        "index.html", "http://localhost:9090"
    );

    TestUtil.assertContentEqualsForGet(
        "index.html", "http://localhost:9090/index.html"
    );
  }

  public void basic() {
    try {
      app.cleanAndBind(
          app ->
              app.get("/", (req, res) -> {
                        WebCtx ctx = req.ctx();
                        ctx.put("user", 1);
                      },
                      (req, res) -> {
                        WebCtx ctx = req.ctx();
                        ctx.put("user", (int) ctx.get("user") + 1);
                      },
                      (req, res) -> {
                        WebCtx ctx = req.ctx();
                        ctx.put("user", (int) ctx.get("user") + 1);
                      },
                      (req, res) -> {
                        Integer result = Convert.toInt(req.ctx().get("user"));
                        res.contentType("application/json;charset=utf8");
                        res.send(200, String.valueOf(result));
                      }
              )
      );


      HTTP.get("http://localhost:9090/", resp ->
          S._try(() -> assertEquals("3", STREAM.readFully(
                                        resp.getEntity().getContent(),
                                        Charset.forName("UTF-8"))
                 )
          ));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void basic_router() {

    Router router = new Router();
    router.get("/add", (req, resp) -> resp.send("add"))
        .get("/del", (req, resp) -> resp.send("del"));

    app.cleanAndBind(
        app ->
            app.get("/", (req, resp) -> resp.send("root"))
                .get("/:id", (req, resp) -> resp.send(req.param("id")))
                .get("/:id/text", (req, resp) -> resp.send("text"))
                .use("/user/*", router)
    );

    try {

      HTTP.get("http://localhost:9090/user/add", null, resp ->
          S._try(() -> assertEquals("add", STREAM.readFully(resp.getEntity().getContent(), utf8))));

      HTTP.get("http://localhost:9090/user/del", null, resp ->
          S._try(() -> assertEquals("del", STREAM.readFully(resp.getEntity().getContent(), utf8))));

      HTTP.get("http://localhost:9090/123", null, resp ->
          S._try(() -> assertEquals("123", STREAM.readFully(resp.getEntity().getContent(), utf8))));

      HTTP.get("http://localhost:9090/123/text", null, resp ->
          S._try(() -> assertEquals("text", STREAM.readFully(resp.getEntity().getContent(), utf8))));

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void basic_min_group_route() {

    S.echo("Testing min_group_route");
    app.cleanAndBind(
        app -> {
          app.get("/:id/new", (req, resp) -> resp.send(req.param("id") + "/new1"));
          app.get("/new/:id", (req, resp) -> resp.send("new2/" + req.param("id")));
          app.get("/new", (req, resp) -> resp.send("new"));
          app.get("/:id", (req, resp) -> resp.send("id=" + req.param("id")));
        });

    try {

      HTTP.get("http://localhost:9090/123/new", null, resp ->
          S._try(() -> assertEquals("123/new1", STREAM.readFully(resp.getEntity().getContent(), utf8))));

      HTTP.get("http://localhost:9090/new/123", null, resp ->
          S._try(() -> assertEquals("new2/123", STREAM.readFully(resp.getEntity().getContent(), utf8))));

      HTTP.get("http://localhost:9090/new", null, resp ->
          S._try(() -> assertEquals("new", STREAM.readFully(resp.getEntity().getContent(), utf8))));

      HTTP.get("http://localhost:9090/233", null, resp ->
          S._try(() -> assertEquals("id=233", STREAM.readFully(resp.getEntity().getContent(), utf8))));

    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  public void basic_ctx() {
    S.echo("Testing ctx_consistency");

    app.cleanAndBind(
        app -> {
          app.use((req, resp) -> {
            S.echo("INSTALLLLLLLLLLLLLLLLLLLLL");
            req.ctx().put("val", 1);
          });

          app.get("/testCtx", (req, resp) -> {
            req.ctx().put("a", "a");
            resp.send(200, req.ctx().get("a").toString() + req.ctx().get("val"));
          });
        }
    );

    try {
      HTTP.get("http://localhost:9090/testCtx", null, resp ->
          S._try(() -> assertEquals("a1", STREAM.readFully(resp.getEntity().getContent(), utf8))));

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void basic_unicode() throws IOException {
    S.echo("Testing utf8");
    app.cleanAndBind(
        app ->
            app.get("/test", (req, resp) -> {
              resp.render(text("中文支持"));
            })

    );
    TestUtil.assertContentEqualsForGet("中文支持", "http://localhost:9090/test");
  }

  class err_ctrl extends Controller {
    @Mapping(value = "/")
    public void err(Request req, Response resp) {
      throw new EndToEndException(400, "用户输入错误");
    }
  }

  public void test_end2end_exception() throws IOException {
    app.cleanAndBind(
        app -> app.get("/err", (req, resp) -> {
          throw new EndToEndException(400, "错误");
        }).use("/err_ctrl/*", new err_ctrl())
    );

    TestUtil.assertContentEqualsForGet("错误", "http://localhost:9090/err");
    TestUtil.assertContentEqualsForGet("用户输入错误", "http://localhost:9090/err_ctrl/");
  }

  @After
  public void stop() {
    app.stop();
  }

}
