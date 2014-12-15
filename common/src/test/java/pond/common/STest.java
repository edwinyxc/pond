package pond.common;


import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class STest {

    final int a = 100;
    final int b = 1000;

    @Test
    public void test_return() throws Exception {

        int c = S._return(() ->  a > b,
                () -> a, () -> b);
        assertEquals(c, b);
    }

    @Test
    public void test_do() throws Exception {
        int[] c = new int[1];
        S._do( ()-> a > b, ()-> c[0] = a, ()-> c[0] = b );
        assertEquals(c[0], b);
    }

    @Ignore
    public void test_http() throws Exception {
        S.http.get("http://www.baidu.com", null, resp -> {
                S.echo(S.time( () -> S._try(() ->S.stream.write(resp.getEntity().getContent(),System.out)) ));
        });
    }

}