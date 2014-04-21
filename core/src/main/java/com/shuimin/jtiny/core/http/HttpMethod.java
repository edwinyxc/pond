package com.shuimin.jtiny.core.http;

import com.shuimin.base.S;

import java.util.List;

public enum HttpMethod {

    GET(1), POST(2), PUT(4), DELETE(8), HEAD(16), OPTIONS(32), TRACE(128), CONNECT(256);

    int value = 0;

    private HttpMethod(int i) {
        value = i;
    }

    public int value() {
        return this.value;
    }

    public boolean match(int test) {
        return (test & this.value) == this.value;
    }

    public static List<HttpMethod> unMask(int i){
        List<HttpMethod> ret = S.list.one();
        for(HttpMethod m : HttpMethod.values()){
            if(m.match(i)){
                ret.add(m);
            }
        }
        return ret;
    }

    public static int mask(HttpMethod... x) {
        int mask = 0;
        for (HttpMethod m : x) {
            mask |= m.value;
        }
        return mask;
    }

    public static HttpMethod of(String method) {
        S._assert(method, "method null");
        if (method.equalsIgnoreCase("get")) {
            return GET;
        }
        if (method.equalsIgnoreCase("post")) {
            return POST;
        }
        if (method.equalsIgnoreCase("put")) {
            return PUT;
        }
        if (method.equalsIgnoreCase("delete")) {
            return DELETE;
        }
        if (method.equalsIgnoreCase("head")) {
            return HEAD;
        }
        if (method.equalsIgnoreCase("trace")) {
            return TRACE;
        }
        if (method.equalsIgnoreCase("options")) {
            return OPTIONS;
        }
        if (method.equalsIgnoreCase("connect")) {
            return CONNECT;
        }
        throw new IllegalArgumentException("method string not recognized");
    }
}
