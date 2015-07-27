package pond.common;

import org.junit.Test;

public class TestHttp {

    static String test_get_url = "http://www.baidu.com";

    @Test
    public void test_http() throws Exception {
        S.echo("Testing http connection " + test_get_url);

        HTTP.get(test_get_url, null, resp -> {
            S.echo(S.time(() -> S._try(() -> S.stream.pipe(resp.getEntity().getContent(), System.out))));
        });
    }
}
