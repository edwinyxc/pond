package com.shuimin.pond.core.spi;

import com.shuimin.common.f.Function;
import com.shuimin.pond.core.ExecutionContext;
import com.shuimin.pond.core.Middleware;

/**
 * Created by ed on 2014/5/7.
 */
public interface MiddlewareExecutor {

    /**
     * <pre>
     *     execute
     * </pre>
     *
     * @param midProvider
     * @return
     */
    ExecutionContext execute(
            Function.F0<ExecutionContext> ctxProvider,
            Function.F0<Iterable<Middleware>> midProvider
    );

}
