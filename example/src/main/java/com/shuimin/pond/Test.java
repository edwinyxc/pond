package com.shuimin.pond;

import static com.shuimin.common.S.echo;

/**
 * Created by ed on 2014/4/11.
 */
public class Test {
    public static void print(String s){
        echo(s);
    }
    public static interface A {
        void test(String a);
    }

    public static void main(String[] args) {
        ((A)Test::print).test("s");
    }
}
