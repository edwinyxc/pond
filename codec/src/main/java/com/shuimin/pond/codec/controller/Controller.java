package com.shuimin.pond.codec.controller;

import com.shuimin.pond.core.ExecutionContext;
import com.shuimin.pond.core.Middleware;
import com.shuimin.pond.core.mw.Dispatcher;
import com.shuimin.pond.core.mw.router.Router;

/**
 * Created by ed on 2014/5/7.
 */
public abstract class Controller extends Dispatcher{

    public Controller() {
        super(new Router() {
            @Override
            public Middleware route(ExecutionContext ctx) {
                return null;
            }

            @Override
            public Router add(int methodMask, String pattern, Middleware... mw) {
                return null;
            }
        });
    }





}
