package com.shuimin.pond.core.spi;

import com.shuimin.common.f.Function;
import com.shuimin.pond.core.ExecutionContext;
import com.shuimin.pond.core.Middleware;
import com.shuimin.pond.core.Request;
import com.shuimin.pond.core.spi.router.RouteNode;

import java.util.LinkedHashMap;

/**
 * @author ed
 */
public interface Router {

    Middleware route(ExecutionContext ctx);

    Router add(int methodMask, String pattern, Middleware... mw);

}
