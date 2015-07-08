package pond.core;

import static pond.common.S.file.loadProperties;

/**
 * Created by ed on 11/13/14.
 */
public class ConfigTest {
    public static void config() throws Exception {
        Pond app = Pond.init( p ->
                p.loadConfig(loadProperties("pond.conf")));

        app.get("/123", (req, resp) ->
                resp.send("<p>"+req.ctx().pond.attr("test")+"</p>"));

        app.listen();
    }
}
