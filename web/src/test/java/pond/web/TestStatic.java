package pond.web;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pond.web.spi.BaseServer;

import java.io.IOException;

/**
 * Created by ed on 15-7-9.
 */
public class TestStatic {

  Pond app;

  @Before
  public void init() {
    app = Pond.init();
    app.config(BaseServer.PORT, "9090");
  }

  @Test
  public void test_bind_root() throws IOException {
    app.get("/.*", app._static("www"));
    app.listen();

    TestUtil.assertContentEqualsForGet(
        "<html><body><p>This is 123.html</p><img src=\"test_lv.jpg\"></body></html>"
        , "http://localhost:9090/123.html"
    );
  }

  @Test
  public void test_bind_non_root() throws IOException {
    app.get("/static/.*", app._static("www"));
    app.listen();

    TestUtil.assertContentEqualsForGet(
        "<html><body><p>This is 123.html</p><img src=\"test_lv.jpg\"></body></html>",
        "http://localhost:9090/static/123.html"
    );
  }

  @Test
  public void test_default_index() throws IOException {
    app.get("/.*", app._static("www"));
    app.listen();

    TestUtil.assertContentEqualsForGet(
        "<html><body>index.html</body></html>",
        "http://localhost:9090"
    );

    TestUtil.assertContentEqualsForGet(
        "<html><body>index.html</body></html>",
        "http://localhost:9090/index.html"
    );
  }

  @After
  public void stop() {
    app.stop();
  }

//  public static void main(String[] args) {
//    Pond.init(p -> p.get("/.*", p._static("www"))).listen(9090);
//  }

}
