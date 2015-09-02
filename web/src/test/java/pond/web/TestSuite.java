package pond.web;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pond.common.*;
import pond.common.f.Callback;
import pond.web.spi.BaseServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

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
    app = Pond.init();
    Pond.config(BaseServer.PORT, "9090");
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
  }

  class DemoController extends Controller {

    int value = 1;

    @Mapping("/read")
    public void read(Request req, Response resp) {
      resp.render(text(String.valueOf(value)));
    }

    @Mapping("/add")
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

  public void controller_bind_controller() throws IOException {
    app.use("/ctrl", new DemoController());

    TestUtil.assertContentEqualsForGet("1", "http://localhost:9090/ctrl/read");
    HTTP.get("http://localhost:9090/ctrl/add",null, Callback.NOOP);
    HTTP.get("http://localhost:9090/ctrl/add",null, Callback.NOOP);
    HTTP.get("http://localhost:9090/ctrl/add",null, Callback.NOOP);
    TestUtil.assertContentEqualsForGet("4", "http://localhost:9090/ctrl/read");
    HTTP.get("http://localhost:9090/ctrl/add/4",null, Callback.NOOP);
    TestUtil.assertContentEqualsForGet("8", "http://localhost:9090/ctrl/read");

  }

  public void controller_bind_controller_to_root() throws IOException {

    S._debug_on(Route.class);

    app.use("", new DemoController());

    TestUtil.assertContentEqualsForGet("1", "http://localhost:9090/read");

  }

  public void multipart() throws IOException {

    app.clean();

    app.post("/multipart", (req, resp) -> {
      Request.UploadFile f = req.file("content");
      try {
        STREAM.pipe(f.inputStream(), resp.out());
      } catch (IOException e) {
        e.printStackTrace();
      }

      resp.send(200);
    });


    File wwwroot = new File(app.config(Pond.CONFIG_WEB_ROOT), "www");

    HTTP.postMultipart(
        "http://localhost:9090/multipart",

        S._tap(new HashMap<>(), map -> {
          map.put("text1", "text1");
          map.put("text2", "text2");
        }),

        S._tap(new HashMap<>(), map -> {
          map.put("content", S._try_ret(() -> new File(wwwroot, "test_lv.jpg")));
        }),

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

    app.clean();

    String json = "{\"a\":\"a\",\"b\":\"b\"}";

    Map jsonMap = JSON.parse(json);

    app.get("/test_render_json", (req, resp) ->
        resp.render(Render.json(jsonMap)));


    TestUtil.assertContentEqualsForGet(json, "http://localhost:9090/test_render_json");
  }

  public void render_text() throws IOException {
    app.clean();

    String text = "sdddaaa";

    app.get("/test_render_text", (req, resp) ->
        resp.render(text(text)));


    TestUtil.assertContentEqualsForGet(text, "http://localhost:9090/test_render_text");
  }


  public void static_bind_root() throws IOException {
    app.clean();
    app.get("/.*", app._static("www"));

    TestUtil.assertContentEqualsForGet(
        "<html><body><p>This is 123.html</p><img src=\"test_lv.jpg\"></body></html>"
        , "http://localhost:9090/123.html"
    );
  }

  public void static_bind_non_root() throws IOException {
    app.clean();
    app.get("/static/.*", app._static("www"));

    TestUtil.assertContentEqualsForGet(
        "<html><body><p>This is 123.html</p><img src=\"test_lv.jpg\"></body></html>",
        "http://localhost:9090/static/123.html"
    );
  }

  public void static_default_index() throws IOException {
    app.get("/.*", app._static("www"));

    TestUtil.assertContentEqualsForGet(
        "<html><body>index.html</body></html>",
        "http://localhost:9090"
    );

    TestUtil.assertContentEqualsForGet(
        "<html><body>index.html</body></html>",
        "http://localhost:9090/index.html"
    );
  }

  public void basic() {
    try {
      app.clean();
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

  public void basic_router() {

    Router router = new Router();
    router.get("/add", (req, resp) -> resp.send("add"))
        .get("/del", (req, resp) -> resp.send("del"));
    app.clean();
    app.get("/", (req, resp) -> resp.send("root"))
        .get("/${id}", (req, resp) -> resp.send(req.param("id")))
        .get("/${id}/text", (req, resp) -> resp.send("text"))
        .use("/user", router);


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
    app.clean();
    app.get("/${id}/new", (req, resp) -> resp.send(req.param("id") + "/new1"));
    app.get("/new/${id}", (req, resp) -> resp.send("new2/" + req.param("id")));
    app.get("/new", (req, resp) -> resp.send("new"));
    app.get("/${id}", (req, resp) -> resp.send("id=" + req.param("id")));

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

    app.clean();
    app.before((req, resp) -> {
      req.ctx().put("val", 1);
    });

    app.get("/testCtx", (req, resp) -> {
      req.ctx().put("a", "a");
      resp.send(200, req.ctx().get("a").toString() + req.ctx().get("val"));
    });

    try {
      HTTP.get("http://localhost:9090/testCtx", null, resp ->
          S._try(() -> assertEquals("a1", STREAM.readFully(resp.getEntity().getContent(), utf8))));

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void basic_unicode() throws IOException {
    S.echo("Testing utf8");
    app.clean();

    app.get("/test", (req, resp) -> {
      resp.render(text("中文支持"));
    });
    TestUtil.assertContentEqualsForGet("中文支持", "http://localhost:9090/test");
  }


  @After
  public void stop() {
    app.stop();
  }

}
