package com.shuimin.pond.core.mw;

import com.shuimin.pond.core.Middleware;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ed
 */
public abstract class AbstractMiddleware implements Middleware {

    private Middleware next;
    private Middleware tail;
    private Map<String, Object> attrs = new HashMap<>();

    @Override
    public Middleware next() {
        return next;
    }

    @Override
    public Middleware next(Middleware ware) {
        next = ware;
        this.tail = ((AbstractMiddleware) ware).tail;
        return next;
    }

    @Override
    public Middleware attr(String name, Object o) {
        this.attrs.put(name, o);
        return this;
    }

    @Override
    public Object attr(String name) {
        return this.attrs.get(name);
    }

    @Override
    public Map<String, Object> attrs() {
        return this.attrs;
    }
}
