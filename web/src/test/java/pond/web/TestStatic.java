package pond.web;

import pond.common.S;
import pond.web.spi.BaseServer;

/**
 * Created by ed on 15-7-9.
 */
public class TestStatic {

  public static void main(String[] args) {
    Pond.init(p -> {
      p.get("/static/.*", p._static("www"));
    }).listen(9090);
  }
}
