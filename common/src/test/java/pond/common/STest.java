package pond.common;


import org.junit.Test;

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
}