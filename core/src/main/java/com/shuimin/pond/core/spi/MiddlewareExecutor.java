package com.shuimin.pond.core.spi;

import com.shuimin.common.f.Function;
import com.shuimin.pond.core.ExecutionContext;
import com.shuimin.pond.core.Middleware;

import java.util.concurrent.Future;

/**
 * Created by ed on 2014/5/7.
 */
public interface MiddlewareExecutor {

    /**
     * <pre>
     *     execute
     * </pre>
     * @param midProvider
     * @return
     */
    Future<ExecutionContext> execute(
        Function._0<ExecutionContext> ctxProvider,
        Function._0<Iterable<Middleware>> midProvider
    );

}
