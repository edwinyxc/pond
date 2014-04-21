package com.shuimin.jtiny.core;

/**
 * @author ed
 */
public abstract class AbstractMiddleware implements Middleware {

    private Middleware next;
    private Middleware tail;

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

}
