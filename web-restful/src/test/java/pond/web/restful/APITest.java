package pond.web.restful;

import org.junit.Before;
import org.junit.Test;
import pond.common.S;
import pond.common.STREAM;
import pond.core.CtxFlowProcessor;
import pond.net.NetServer;
import pond.net.ServerConfig.ServerConfigBuilder;
import pond.web.http.HttpConfigBuilder;
import pond.web.router.Router;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

public class APITest {
    HttpConfigBuilder app = (HttpConfigBuilder) new HttpConfigBuilder().port(9090);
    NetServer server = new NetServer(app);
    Charset utf8 = Charset.forName("UTF-8");

    CtxFlowProcessor singleThreadProcessor = new CtxFlowProcessor("Hello").executor(Executors.newSingleThreadExecutor());

    @Before
    public void init() throws InterruptedException {
        server.listen(9090);
    }

    public void test_ParamDef_array() throws IOException, URISyntaxException, InterruptedException {
        app.clean().handler( new Router(app -> {
                    app.get("/api/:path_array/inpath", API.def(
                            ParamDef.arrayInPath("path_array"),
                            ResultDef.text("echo"),
                            (ctx, pathArr, Echo) -> {
                                ctx.result(Echo, S.dump(pathArr)+":"+pathArr.size());
                            }
                    ).flowTo(singleThreadProcessor));

                    app.get("/api/getEntry", API.def(
                            ParamDef.arrayInQuery("q"),
                            ResultDef.text("echo"),
                            (ctx, qArr, Echo) -> {
                                ctx.result(Echo, S.dump(qArr)+":"+qArr.size());
                            }
                    ).flowTo(singleThreadProcessor));

                    app.post("/api/post", API.def(
                            ParamDef.arrayInForm("q"),
                            ResultDef.text("echo"),
                            (ctx, qArr, Echo) -> {
                                ctx.result(Echo, S.dump(qArr)+":"+qArr.size());
                            }
                    ).flowTo(singleThreadProcessor));
                }
        ));

        TestUtil.assertContentEqualsForGet("[1,2,3]:3", "http://localhost:9090/api/1,2,3/inpath");
        TestUtil.assertContentEqualsForGet("[1,2,3]:3", "http://localhost:9090/api/getEntry?q=1,2,3");
        TestUtil.assertContentEqualsForGet("[1,2,3]:3", "http://localhost:9090/api/getEntry?q=1&q=2&q=3");

        /*
        HTTP.post("http://localhost:9090/api/post", new HashMap<String, Object>(){{
            put("q", "1,2,3");
        }}, resp -> S._try(() -> assertEquals("[1,2,3]:3", STREAM.readFully(resp.getEntity().getContent(), utf8))));
        */

//    HTTP.post("http://localhost:9090/post", new HashMap<String, Object>(){{
//          put("q", new String[]{"1","2","3"});
//    }}, resp -> S._try(() -> assertEquals("1,2,3:3", STREAM.readFully(resp.getEntity().getContent(), utf8))));

    }

    @Test
    public void test() throws Exception {
        test_ParamDef_array();
        server.stop();
    }

}