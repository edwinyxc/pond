package pond.web.restful;

import io.netty.util.CharsetUtil;
import org.apache.http.Header;
import pond.common.HTTP;
import pond.common.S;
import pond.common.STREAM;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by ed on 8/24/15.
 */
public class TestUtil {

  public static void assertContentEqualsForGet(String judge, String url) throws IOException {
    assertNotNull(judge);
    HTTP.get(url, null, resp ->
        S._try(() -> assertEquals(new String(judge.trim().getBytes(), CharsetUtil.UTF_8),
                                  STREAM.readFully(resp.getEntity().getContent(), CharsetUtil.UTF_8).trim()
               )
        ));
  }

  public static void assertHeaderContains(String judge, String url, String name) throws IOException {

    HTTP.get(url, null, resp -> {
      Header[] s = resp.getHeaders(name);
      assertTrue(S._for(s).some(header -> header.getValue().contains(judge)));
    });

  }


}
