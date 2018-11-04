package pond.web.restful;

import io.netty.util.CharsetUtil;
import pond.common.S;
import pond.common.STREAM;
import pond.core.CtxHandler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.Assert.*;

/**
 * Created by ed on 8/24/15.
 */
public class TestUtil {
    static HttpClient client = HttpClient.newBuilder().build();

  public static void assertContentEqualsForGet(String judge, String url) throws IOException, URISyntaxException, InterruptedException {
    assertNotNull(judge);
    HttpRequest request = HttpRequest.newBuilder(new URI(url)).build();
    var body = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    assertEquals(judge, body);
  }

  public static void assertHeaderContains(String judge, String url, String name) throws IOException, URISyntaxException, InterruptedException {
    assertNotNull(judge);
    HttpRequest request = HttpRequest.newBuilder(new URI(url)).build();
    var headers = client.send(request, HttpResponse.BodyHandlers.ofString()).headers();
    assertEquals(judge, headers.firstValue(name).orElse(null));
  }

//  static void _proxy() throws IOException {
//    Pond.init(API.class, p -> {
//      p.use("/file/*", CtxHandler.proxyEntireSite("http://localhost:9333/"));
//    }).listen(9090);
//
//    new Thread(() -> {
//      Pond.init(API.class, p -> {
//        p.get("/*", p._static("www")).otherwise(InternalMids.FORCE_CLOSE);
//      }).listen(9333);
//    }).run();
//  }
//
//  public static void main (String[] args) throws IOException{
//    _proxy();
//  }


}
