package pond.core;

import java.util.Properties;

/**
 * Created by ed on 10/31/14.
 */
public class TestAppConfig {


    public static void main(String[] args) {
        Pond app = Pond.init(p -> p.loadConfig(new Properties()),
                             p -> p._static("www"),
                             p -> {
                                 p.get("/123", (req, res) -> res.send("123"))
                                  .get("/234", (req, res) -> res.send("234"));
                             }).listen();
    }

}
