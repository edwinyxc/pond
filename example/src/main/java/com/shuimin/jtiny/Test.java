package com.shuimin.jtiny;

import static com.shuimin.base.S.echo;

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
