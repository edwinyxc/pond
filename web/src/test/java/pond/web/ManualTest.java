package pond.web;

import pond.common.HTTP;
import pond.common.S;
import pond.common.f.Tuple;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import static pond.web.Render.text;

/**
 * Created by ed on 9/2/15.
 */
public class ManualTest {

  static Charset utf8 = Charset.forName("UTF-8");

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


  static
  class err_ctrl extends Controller {
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

  public static void main(String[] args) throws IOException {

    test_end2end_exception();

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
