package pond.web;

import pond.common.HTTP;
import pond.common.S;
import pond.common.f.Callback;

import java.io.IOException;

import static pond.web.Render.text;

/**
 * Created by ed on 9/2/15.
 */
public class ManualTest {

  static void a() {
    Pond app = Pond.init();

    app.use((req, resp) -> {
      req.ctx().put("val", 1);
    });

    app.get("/testCtx", (req, resp) -> {
      req.ctx().put("a", "a");
      resp.send(200, req.ctx().get("a").toString() + req.ctx().get("val"));
    });
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


  static void test_require() {
    Pond app = Pond.init().debug().listen(9090);
    Mid session = Session.install();

    app.cleanAndBind(p -> {

      p.get("/require",
            session,
            Mid.wrap((req, resp) -> resp.send(200, "pass")).require(session));

      p.get("/requireFail",
            Mid.wrap((req, resp) -> resp.send(200, "pass")).require(session));

    });
  }

  static class DemoController extends Controller {

    int value = 1;

    @Mapping("/")
    public void root(Request req, Response resp) {
      resp.send(200,"root");
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

  public static void main(String[] args) throws IOException {

    controller_bind_controller();

//    b();
//    test_router();
//    test();
//    test_require();

  }

}
