package com.shuimin.pond.core.mw;

import com.shuimin.pond.core.AbstractMiddleware;
import com.shuimin.pond.core.ExecutionContext;
import com.shuimin.pond.core.Server;

/**
 * Created by ed on 2014/4/30.
 * <p>Plugin is a kind of special Middleware that represent
 * </p>
 */
public abstract class Plugin extends AbstractMiddleware {

    public abstract void install();

    @Override
    public ExecutionContext handle(ExecutionContext ctx) {
        return ctx;
    }

    @Override
    public void init() {
        //register singleton
        Server.register(this.getClass(), this);
        install();
    }

    public static Object param(Class<? extends Plugin> clazz,String name) {
        Object s = Server.register(clazz);
        if(s == null ) return null;
        return ((Plugin) s).attr(name);
    }

}
