package com.shuimin.base.java8;

import com.shuimin.base.S;

import java.util.Map;

import static com.shuimin.base.S.*;

public class LambdaTest {

    public static void main(String[] args) {

        Map<Integer, String> testMap = map.hashMap(new Object[][]{
            {1, "one"}, {2, "two"}, {3, "weee"}, {4, "weee"}
        });

        _for(testMap).map((s) -> s + "_new").each((entry) -> echo(entry.getValue()));

        for (int i = 0; i < 100; i++) {
            testMap.put(i, String.valueOf(i));
        }

        _for(testMap).grep((entry) -> (entry.getKey() > 50)).each(S::echo);

//        List<Integer> list = collection.list.arrayList(
//                new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 23, 23, 2, 2, 3, 23, 22});

    }
}
