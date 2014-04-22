package com.shuimin.pond.core.http;

/**
 * Created by ed on 2014/4/10.
 */
public class HttpHeader {
    private final String name;
    private final String[] values;

    public HttpHeader(String name, String[] values) {
        this.name = name;
        this.values = values;
    }

}
