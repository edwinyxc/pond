package com.shuimin.pond.core.spi.router;

import com.shuimin.pond.core.ExecutionContext;
import com.shuimin.pond.core.Middleware;
import com.shuimin.pond.core.Request;


/**
 * Created by ed interrupt 2014/4/2.
 */
public interface RouteNode extends Middleware {
    static RouteNode of(String path, Middleware ware) {
        return new RouteNode() {

            @Override
            public ExecutionContext handle(ExecutionContext ctx) {
                return ware.handle(ctx);
            }

            @Override
            public Middleware next() {
                return ware.next();
            }

            @Override
            public Middleware next(Middleware ware) {
                return ware.next(ware);
            }

            @Override
            public ExecutionContext exec(ExecutionContext ctx) {
                return ware.exec(ctx);
            }

            @Override
            public boolean match(Request req) {
                return new RegexPathMatcher(path).apply(req);
            }
        };
    }

    /**
     * true if req math the path
     *
     * @param req - request that contains path
     * @return boolean
     */
    public boolean match(Request req);
}
