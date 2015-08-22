package pond.web;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pond.common.HTTP;
import pond.common.S;
import pond.common.f.Callback;
import pond.web.spi.BaseServer;

import java.io.IOException;
import java.nio.charset.Charset;

import static pond.web.Render.text;

public class TestController {


  Pond app;

  Charset utf8 = Charset.forName("UTF-8");


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

  @Before
  public void before() {
    app = Pond.init();
    Pond.config(BaseServer.PORT, "9090");
  }

  @Test
  public void test_bind_controller() throws IOException {
    app.use("/ctrl", new DemoController());

    app.listen();

    TestUtil.assertContentEqualsForGet("1", "http://localhost:9090/ctrl/read");
    HTTP.get("http://localhost:9090/ctrl/add",null, Callback.NOOP);
    HTTP.get("http://localhost:9090/ctrl/add",null, Callback.NOOP);
    HTTP.get("http://localhost:9090/ctrl/add",null, Callback.NOOP);
    TestUtil.assertContentEqualsForGet("4", "http://localhost:9090/ctrl/read");
    HTTP.get("http://localhost:9090/ctrl/add/4",null, Callback.NOOP);
    TestUtil.assertContentEqualsForGet("8", "http://localhost:9090/ctrl/read");

  }

  @Test
  public void test_bind_controller_to_root() throws IOException {

    S._debug_on(Route.class);

    app.use("", new DemoController());

    app.listen();

    TestUtil.assertContentEqualsForGet("1", "http://localhost:9090/read");

  }

  @After
  public void close() {
    app.stop();
  }

}
