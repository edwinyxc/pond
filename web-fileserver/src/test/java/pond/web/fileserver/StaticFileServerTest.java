package pond.web.fileserver;

import org.junit.Ignore;
import org.junit.Test;
import pond.common.S;
import pond.net.NetServer;
import pond.web.http.HttpConfigBuilder;
import pond.web.router.Router;

import java.util.concurrent.ExecutionException;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;

public class StaticFileServerTest {

    @Test @Ignore
    public void watch() throws ExecutionException, InterruptedException {
        new NetServer(new HttpConfigBuilder()
                          .handler(new Router(app -> {
                              app.use("/*", new DefaultStaticFileServer("www"));
                          }))).listen(8080).get();
    }
}