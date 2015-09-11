package pond.web;

import pond.common.S;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

      p.use(Session.install);

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
    app.cleanAndBind(p -> {

      p.get("/require",
            Session.install,
            Mid.wrap((req, resp) -> resp.send(200, "pass")).require(Session.install)
      );

      p.get("/requireFail",
            Mid.wrap((req, resp) -> resp.send(200, "pass")).require(Session.install)
      );

    });
  }

  public static void main(String[] args) {
//    b();
//    test_router();
//    test();

//    test_require();
  }

}
