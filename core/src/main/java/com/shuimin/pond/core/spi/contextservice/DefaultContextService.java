package com.shuimin.pond.core.spi.contextservice;

import com.shuimin.pond.core.ExecutionContext;
import com.shuimin.pond.core.spi.ContextService;

/**
 * Created by ed on 2014/5/8.
 */
public class DefaultContextService implements ContextService{
    public ThreadLocal<ExecutionContext> contextThreadLocal = new ThreadLocal<>();
    @Override
    public ExecutionContext get() {
        return contextThreadLocal.get();
    }

    @Override
    public void set(ExecutionContext ctx) {
        contextThreadLocal.set(ctx);
    }

    @Override
    public ExecutionContext remove(ExecutionContext ctx) {
        contextThreadLocal.remove();
        return ctx;
    }
}
