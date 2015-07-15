package pond.common;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static pond.common.S._for;

public class ForTest {

    @Test
    public void testArrayMapping() {
        String[] testee = new String[]{"a", "b", "c"};
        String[] result = _for(testee).map((t) -> t + t).join();
        assertArrayEquals(result, new String[]{"aa", "bb", "cc"});
    }


    @Test
    public void testIterableMapping() {

        Iterable<String> testee = new ArrayList(){
            {
                this.add("aa");
                this.add("bb");
                this.add("cc");
            }
        };

        assertArrayEquals(_for(testee).map(t -> "a").join(), new String[]{"a", "a", "a"});
    }

    @Test
    public void testReduce() {

        Integer[] arr = {1, 2, 3, 4, 5, 6, 6, 6344, 3, 2, 2, 3, 4};

        assertEquals((long) _for(arr).map(i -> i + 1).reduce((a, b) -> a > b ? a : b), 6345);
    }

    @Test
    public void testReduceWithHead() {

        Integer[] arr = {1, 2, 3, 4, 5, 6, 6, 6344, 3, 2, 2, 3, 4};
        assertEquals((int) _for(arr).reduce((acc, r) -> acc + r, 0), 6384);
    }


}
