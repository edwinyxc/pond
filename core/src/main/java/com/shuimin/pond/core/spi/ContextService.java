package com.shuimin.pond.core.spi;

import com.shuimin.pond.core.ExecutionContext;

/**
 * Created by ed on 2014/5/8.
 */
public interface ContextService {

    ExecutionContext get();

    void set(ExecutionContext ctx);

    ExecutionContext remove(ExecutionContext ctx);

}
