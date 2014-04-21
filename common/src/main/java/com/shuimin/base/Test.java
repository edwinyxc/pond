package com.shuimin.base;

public class Test {

    static void test() {
    }

    public static void main(String[] s) {
        String a = null;
        char c = S._avoidNull(a, String.class).charAt(0);
        System.out.println(c);
    }
}
