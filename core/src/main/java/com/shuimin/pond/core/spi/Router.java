package com.shuimin.pond.core.spi;

import com.shuimin.pond.core.ExecutionContext;
import com.shuimin.pond.core.Middleware;

/**
 * @author ed
 */
public interface Router {

    Middleware route(ExecutionContext ctx);

    Router add(int methodMask, String pattern, Middleware... mw);

}
