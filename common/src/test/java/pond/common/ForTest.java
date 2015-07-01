package pond.common;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static pond.common.S._for;
import static pond.common.S.echo;

public class ForTest {

    public void testArray() {
        _for(new String[]{"a", "b", "c"})
                //.map((a) -> a.codePointAt(0))
                //.filter((a) -> a < 100)
                .map((t) -> {
                    S.echo(t);
                    return String.valueOf(t);
                })
                .each(S::echo);
        String[] tmp = new String[]{"a", "b", "c"};
        List list = Arrays.asList(tmp);
    }

    public void testMap() {

        Map<String, Integer> map = S.map
                .<String, Integer>hashMap(new Object[][]{{"one", 1},
                        {"two", 2}});
        _for(map).<String>map((a) -> ("" + a)).filterByValue((a) -> {
            S.echo(a);
            return true;
        }).each((entry) -> S.echo(entry.getValue()));
    }

    public void testBi() {

        Integer[] arr = {1, 2, 3, 4, 5, 6, 6, 6344, 3, 2, 2, 3, 4};

        echo(_for(arr).map(i -> i + 1).reduce((a, b) -> a > b ? a : b));

    }

}
