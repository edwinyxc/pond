package pond.common;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class STest {

    @Test
    public void test_return() throws Exception {
        final int a = 100;
        final int b = 1000;

        int c = S._return(() -> a > b,
                () -> a, () -> b);
        assertEquals(c, b);
    }

    @Test
    public void test_do() throws Exception {
        final int a = 100;
        final int b = 1000;
        int[] c = new int[1];
        S._do(() -> a > b, () -> c[0] = a, () -> c[0] = b);
        assertEquals(c[0], b);
    }

    public void test_http() throws Exception {
        S.echo("testing http");
        S.http.get("http://www.baidu.com", null, resp -> {
            S.echo(S.time(() -> S._try(() -> S.stream.pipe(resp.getEntity().getContent(), System.out))));
        });
    }

    @Test(expected = Exception.class)
    public void test_fail() throws Exception {
        S._fail();
    }

    @Test
    public void test_avoidNull() throws Exception {
        assertEquals("else", S.avoidNull(null, "else"));
    }

    @Test
    public void test_array_join() {
        String[] arr = S.array.of(new ArrayList<String>() {{
            this.add("a");
            this.add("b");
            this.add("c");
            this.add("d");
        }});
        assertArrayEquals(new String[]{"a", "b", "c", "d"}, arr);
    }

    @Test
    public void test_in() {
        assertEquals(S._in("a", "a", "b", "c"), true);
    }

    @Test(expected = Exception.class)
    public void test_try_ret(){
        String a =  S._try_ret(() -> {
            throw new Exception();
        });
    }

    @Test
    public void test_try(){
        S._try(() -> S.echo("sd"));
    }

}
