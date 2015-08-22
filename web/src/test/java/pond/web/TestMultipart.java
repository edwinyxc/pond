package pond.web;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import pond.common.HTTP;
import pond.common.S;
import pond.common.STREAM;
import pond.web.spi.BaseServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;

import static pond.common.S.dump;
import static pond.web.Render.json;

/**
 * Created by ed on 15-7-9.
 */
public class TestMultipart {

  Pond app;

  Charset utf8 = Charset.forName("UTF-8");

  @Before
  public void init(){
    app = Pond.init().debug();
    app.config(BaseServer.PORT, "9090");
  }

  @Test
  public void test_multipart() throws IOException {

    app.post("/multipart", (req, resp) -> {
      Request.UploadFile f = req.file("content");
      try {
        STREAM.pipe(f.inputStream(), resp.out());
      } catch (IOException e) {
        e.printStackTrace();
      }

      resp.send(200);
    });

    app.listen();

    File wwwroot = new File(app.config(Pond.CONFIG_WEB_ROOT),"www");

    HTTP.postMultipart("http://localhost:9090/multipart",
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

  @After
  public void stop() {
    app.stop();
  }

  public static void main(String[] args) {
    S._debug_on(Pond.class, BaseServer.class);
    System.setProperty(BaseServer.PORT, "9090");
    Pond.init(p -> {
      p.post("/multipart", (req, resp) -> {
        Request.UploadFile f = req.file("content");
        resp.render(json("<pre>" + dump(req) +
                             "</pre><br><pre>" + dump(f) + "</pre>"));
        resp.send(200, "OK");
      }).get("/.*", p._static("www"));
    }).listen();
  }
}
