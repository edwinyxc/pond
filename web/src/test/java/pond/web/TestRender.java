package pond.web;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pond.common.JSON;
import pond.web.spi.BaseServer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestRender {

  Pond app;

  Charset utf8 = Charset.forName("UTF-8");

  @Before
  public void before() {
    app = Pond.init();
    Pond.config(BaseServer.PORT,"9090");
  }

  @Test
  public void test_render_json() throws IOException {

    String json = "{\"a\":\"a\",\"b\":\"b\"}";

    Map jsonMap = JSON.parse(json);

    app.get("/test_render_json", (req, resp) ->
        resp.render(Render.json(jsonMap)));

    app.listen();

    TestUtil.assertContentEqualsForGet(json, "http://localhost:9090/test_render_json");
  }

  @Test
  public void test_render_text() throws IOException {
    String text = "sdddaaa";

    app.get("/test_render_text", (req, resp) ->
        resp.render(Render.text(text)));

    app.listen();

    TestUtil.assertContentEqualsForGet(text, "http://localhost:9090/test_render_text");
  }

  @After
  public void close() {
    app.stop();
  }

}
