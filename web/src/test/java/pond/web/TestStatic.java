package pond.web;

import pond.common.S;
import pond.web.spi.BaseServer;

/**
 * Created by ed on 15-7-9.
 */
public class TestStatic {

  public static void main(String[] args) {
    S._debug_on(Pond.class, BaseServer.class);
    System.setProperty(BaseServer.PORT, "9090");
    Pond.init(p -> {
      p.get("/123.html", (req, resp) -> {
        resp.send("wwwwwwwwwwwwww" +
                      "wwwwwwwwwwwwwwwwwwwww" +
                      "wwwwwwwwwwwwwwwwwwwwww" +
                      "wwwwwwwwwwwwwwwwwwwwww" +
                      "wwwwwwwwwwwwwwwwwwwwww" +
                      "wwwwwwwwwwwwwwwwwwwwww" +
                      "wwwwwwwwwwwwwwwwwwwwww" +
                      "wwwwwwwwwwwwwwwwwwwwww" +
                      "wwwwwwwwwwwwwwwwwwwwww" +
                      "wwwwwwwwwwwwwwwwwwwwwwwwww" +
                      "中文测试" +

                      "");
      }).get("/static/.*", p._static("www"));
    }).listen();
  }
}
