package pond.web;

import pond.common.S;
import pond.web.spi.BaseServer;

/**
 * Created by ed on 15-7-9.
 */
public class TestUTF8 {

  public static void main(String[] args) {
    S._debug_on(Pond.class, BaseServer.class);
    System.setProperty(BaseServer.PORT, "9090");
    Pond.init(p -> {
      p.post("/utf8test", (req, resp) -> {
        S.echo(S.dump(req.params()));
        resp.render(Render.dump(req.params()));
      }).get("/.*", p._static("www"));
    }).listen();
  }
}
