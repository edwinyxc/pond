package com.shuimin.common;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.shuimin.common.S._for;
import static com.shuimin.common.S.echo;

public class ForTest extends TestCase {

    public static void testArray() {
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

    public static void testMap() {

        Map<String, Integer> map = S.map
                .<String, Integer>hashMap(new Object[][]{{"one", 1},
                        {"two", 2}});
        _for(map).<String>map((a) -> ("" + a)).filterByValue((a) -> {
            S.echo(a);
            return true;
        }).each((entry) -> S.echo(entry.getValue()));
    }

    public static void testBi() {

        Integer[] arr = {1, 2, 3, 4, 5, 6, 6, 6344, 3, 2, 2, 3, 4};

        echo(_for(arr).map(i -> i + 1).reduce((a, b) -> a > b ? a : b));

    }

    public static void main(String[] args) {
//        testArray();
        testBi();


    }
}
