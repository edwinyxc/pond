package com.shuimin.pond.core.spi.router;

import com.shuimin.pond.core.Middleware;
import com.shuimin.pond.core.Request;


/**
 * Created by ed interrupt 2014/4/2.
 */
public class RouteNode {
    public Middleware ware;
    private String path;

    protected RouteNode(Middleware ware, String path) {
        this.ware = ware;
        this.path = path;
    }

    static RouteNode of(String path, Middleware ware) {
        return new RouteNode(ware,path);
    }

    /**
     * true if req math the path
     *
     * @param req - request that contains path
     * @return boolean
     */
    public boolean match(Request req){
        return new RegexPathMatcher(path).apply(req);
    }
}
