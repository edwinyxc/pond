package pond.web;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pond.common.Convert;
import pond.common.HTTP;
import pond.common.S;
import pond.common.STREAM;

import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;

/**
 * Created by ed on 8/22/15.
 */
public class TestBasic {

  Pond app;

  Charset utf8 = Charset.forName("UTF-8");

  @Before
  public void init() {
    app = Pond.init();
  }

  @Test
  public void basic() {
    try {

      app.get("/", (req, res) -> {
                Ctx ctx = req.ctx();
                ctx.put("user", 1);
              },
              (req, res) -> {
                Ctx ctx = req.ctx();
                ctx.put("user", (int) ctx.get("user") + 1);
              },
              (req, res) -> {
                Ctx ctx = req.ctx();
                ctx.put("user", (int) ctx.get("user") + 1);
              },
              (req, res) -> {
                Integer result = Convert.toInt(req.ctx().get("user"));
                res.contentType("application/json;charset=utf8");
                res.send(200, String.valueOf(result));
              }
      );
      app.listen(9090);

      HTTP.get("http://localhost:9090/", null, resp ->
          S._try(() -> assertEquals("3", STREAM.readFully(
                                        resp.getEntity().getContent(),
                                        Charset.forName("UTF-8"))
                 )
          ));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void test_router() {

    Router router = new Router();
    router.get("/add", (req, resp) -> resp.send("add"))
        .get("/del", (req, resp) -> resp.send("del"));

    app.get("/", (req, resp) -> resp.send("root"))
        .get("/${id}", (req, resp) -> resp.send(req.param("id")))
        .get("/${id}/text", (req, resp) -> resp.send("text"))
        .use("/user", router);

    app.listen(9090);

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

  @Test
  public void test_min_group_route() {

    S.echo("Testing min_group_route");

    app.get("/${id}/new", (req, resp) -> resp.send(req.param("id") + "/new1"));
    app.get("/new/${id}", (req, resp) -> resp.send("new2/" + req.param("id")));
    app.get("/new", (req, resp) -> resp.send("new"));
    app.get("/${id}", (req, resp) -> resp.send("id=" + req.param("id")));
    app.listen(9090);

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


  @Test
  public void test_ctx() {
    S.echo("Testing ctx_consistency");

    //TODO
    app.before((req, resp) -> {
      req.ctx().put("val", 1);
    });

    app.get("/testCtx", (req, resp) -> {
      req.ctx().put("a", "a");
      resp.send(200, req.ctx().get("a").toString() + req.ctx().get("val"));
    });
    app.listen(9090);

    try {
      HTTP.get("http://localhost:9090/testCtx", null, resp ->
          S._try(() -> assertEquals("a1", STREAM.readFully(resp.getEntity().getContent(), utf8))));

    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  @After
  public void stop() {
    app.stop();
  }
}
