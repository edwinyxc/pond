package pond.web;

import java.util.Properties;

/**
 * Created by ed on 10/31/14.
 */
public class TestAppConfig {


  public static void main(String[] args) {

    Pond.init(p -> p.loadConfig(new Properties()),
              p -> p.get("/123", (req, res) -> res.send("123"))
                  .get("/234", (req, res) -> res.send("234")),
              p -> p.get("/.*", p._static("www"))
    ).listen();
  }

}
