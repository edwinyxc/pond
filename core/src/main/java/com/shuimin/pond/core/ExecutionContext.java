package com.shuimin.pond.core;

import com.shuimin.common.abs.Attrs;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author ed
 */
public class ExecutionContext implements Attrs<ExecutionContext> {

    private final Map<String, Object> attrs = new TreeMap<>();

    @Override
    public ExecutionContext attr(String name, Object o) {
        attrs.put(name, o);
        return this;
    }

    @Override
    public Object attr(String name) {
        return attrs.get(name);
    }

    @Override
    public Map<String, Object> attrs() {
        return attrs;
    }

    private Request req;

    private Response resp;

    private Object last;

    public ExecutionContext take(Request req, Response resp) {
        this.req = req;
        this.resp = resp;
        return this;
    }

    private ExecutionContext() {
    }

    static ExecutionContext init(Request req, Response resp) {
        return new ExecutionContext().take(req, resp);
    }

    public Request req() {
        return req;
    }

    public Response resp() {
        return resp;
    }

    /**
     * last result holder
     *
     * @return return value of last execution
     */
    public Object last() {
        return last;
    }

    public ExecutionContext next(Object value) {
        this.last = value;
        return this;
    }

    public void remove(){

    }
}

