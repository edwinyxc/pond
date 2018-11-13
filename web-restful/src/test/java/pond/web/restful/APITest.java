//package pond.web.restful;
//
//import org.junit.Before;
//import org.junit.Test;
//import pond.common.S;
//import pond.common.STREAM;
//import pond.web.Pond;
//
//import java.io.IOException;
//import java.nio.charset.Charset;
//import java.util.HashMap;
//
//import static org.junit.Assert.*;
//
//public class APITest {
//    Pond app;
//
//    Charset utf8 = Charset.forName("UTF-8");
//
//    @Before
//    public void init() {
//        app = Pond.init().debug();
//        S.config.set(BaseServer.class, BaseServer.PORT, "9090");
////    System.setProperty("file.encoding","utf8");
//        app.listen();
//    }
//
//    public void test_ParamDef_array() throws IOException {
//        app.cleanAndBind( app -> {
//                    app.get("/api/:path_array/inpath", API.def(
//                            ParamDef.arrayInPath("path_array"),
//                            ResultDef.text("echo"),
//                            (ctx, pathArr, Echo) -> {
//                                ctx.result(Echo, S.dump(pathArr)+":"+pathArr.size());
//                            }
//                    ));
//
//                    app.get("/api/get", API.def(
//                            ParamDef.arrayInQuery("q"),
//                            ResultDef.text("echo"),
//                            (ctx, qArr, Echo) -> {
//                                ctx.result(Echo, S.dump(qArr)+":"+qArr.size());
//                            }
//                    ));
//
//                    app.post("/api/post", API.def(
//                            ParamDef.arrayInForm("q"),
//                            ResultDef.text("echo"),
//                            (ctx, qArr, Echo) -> {
//                                ctx.result(Echo, S.dump(qArr)+":"+qArr.size());
//                            }
//                    ));
//                }
//        );
//
//        TestUtil.assertContentEqualsForGet("[1,2,3]:3", "http://localhost:9090/api/1,2,3/inpath");
//        TestUtil.assertContentEqualsForGet("[1,2,3]:3", "http://localhost:9090/api/get?q=1,2,3");
//        TestUtil.assertContentEqualsForGet("[1,2,3]:3", "http://localhost:9090/api/get?q=1&q=2&q=3");
//
//        HTTP.post("http://localhost:9090/api/post", new HashMap<String, Object>(){{
//            put("q", "1,2,3");
//        }}, resp -> S._try(() -> assertEquals("[1,2,3]:3", STREAM.readFully(resp.getEntity().getContent(), utf8))));
//
////    HTTP.post("http://localhost:9090/post", new HashMap<String, Object>(){{
////          put("q", new String[]{"1","2","3"});
////    }}, resp -> S._try(() -> assertEquals("1,2,3:3", STREAM.readFully(resp.getEntity().getContent(), utf8))));
//
//    }
//
//    @Test
//    public void test() throws IOException {
//        test_ParamDef_array();
//        app.stop();
//    }
//
//}