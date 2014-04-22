package com.shuimin.common.s;

import com.shuimin.common.S;

import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author ed
 */
public class STest {

    static void test_array_join() {
        S.echo(S.array.of(new ArrayList<String>(){{
                this.add("s");
                this.add("b");
                this.add("b");
                this.add("b");
        }}));
    }

    static void test_lazyThrow() {
        String a = "";
        try {
            a
                = throwCheckedException();
        } catch (IOException ex) {
            S._lazyThrow(ex);
        }
        S.echo(a);
    }

    static String throwCheckedException() throws IOException {
        throw new IOException("something wrong");
    }

    static String test_requireNonNull() {
        return S._notNullElse(null, "default");
    }

    static void test_avoidNull(Object possibleNull) {
        S.echo(S._avoidNull((String) possibleNull, String.class) + "ok");
    }

    static void test_fail() {
        S._fail();
    }

    static void test_assert(String v) {
        S._assert(v != null, "v must not null");
        S.echo(v.endsWith("sdd"));
    }

    public static void main(String args[]) {
//		test_assert(null);
//		test_fail();
//		test_avoidNull(null);
//		S.echo(test_requireNonNull());
//		S.time();
//        Map<String, Integer> map = S.map.hashMap(new Object[][]{
//            {"one", 1},
//            {"two", 2}
//        });
//        List<Integer> mylist = S.collection.list.arrayList(new Integer[]{1, 2, 3, 4});
//        List<Integer> list = Arrays.asList(new Integer[]{1, 2, 3, 4});
//
//        S.echo(map.get("one"));
//		w


        test_array_join();
    }
}

