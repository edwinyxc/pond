package com.shuimin.pond.core.mw;

import com.shuimin.common.f.Callback;
import com.shuimin.common.f.Function;
import com.shuimin.pond.core.*;

import java.util.Map;


/**
 * @author ed
 */
public abstract class Action extends AbstractMiddleware {


    /**
     * <p>最简单的ACtion，接受一对req,resp 作为参数而不返回信息</p>
     *
     * @param cb
     * @return
     */
    public static Action simple(Callback.C2<Request, Response> cb) {
        return new Action() {
            @Override
            public ExecutionContext handle(ExecutionContext ctx) {
                cb.apply(ctx.req(), ctx.resp());
                return ctx;
            }

        };
    }

    /**
     * <p>消耗上一个action返回的对象 T ,不返回信息</p>
     *
     * @param cb
     * @param <T>
     * @return
     */
    public static <T> Action consume(Callback<T> cb) {
        return new Action() {

            @Override
            public ExecutionContext handle(ExecutionContext ctx) {
                T val = (T) ctx.last();
                cb.apply(val);
                return ctx;
            }
        };
    }

    /**
     * <p>处理单个输入，返回另一个对象</p>
     *
     * @param converter
     * @param <R>
     * @param <T>
     * @return
     */
    public static <R, T> Action process(Function<R, T> converter) {
        return new Action() {

            @Override
            public ExecutionContext handle(ExecutionContext ctx) {
                return ctx.next(converter.apply((T) ctx.last()));
            }

        };
    }

    /**
     * <p>提供一个输出</p>
     *
     * @param cb
     * @param <T>
     * @return
     */
    public static <T> Action supply(Function.F0<T> cb) {
        return new Action() {

            @Override
            public ExecutionContext handle(ExecutionContext ctx) {
                return ctx.next(cb.apply());
            }

        };
    }

    /**
     * <p>从 req,resp 中提供输出</p>
     *
     * @param cb
     * @param <T>
     * @return
     */
    public static <T> Action resolve(Function.F2<T, Request, Response> cb) {
        return new Action() {

            @Override
            public ExecutionContext handle(ExecutionContext ctx) {
                return ctx.next(cb.apply(ctx.req(), ctx.resp()));
            }

        };
    }

    /**
     * <p>直接暴露executionContext</p>
     *
     * @param cb
     * @return
     */
    public static Action raw(Function<ExecutionContext, ExecutionContext> cb) {
        return new Action() {

            @Override
            public ExecutionContext handle(ExecutionContext ctx) {
                return cb.apply(ctx);
            }

        };
    }

    /**
     * <p>一般作为流程最后一步</p>
     *
     * @param cb
     * @param <T>
     * @return
     */
    public static <T> Action end(Callback<T> cb) {
        return new Action() {

            @Override
            public ExecutionContext handle(ExecutionContext ctx) {
                cb.apply((T) ctx.last());
                return null;
            }

        };
    }

    public static Action end(Callback.C2<Request, Response> cb) {
        return new Action() {

            @Override
            public ExecutionContext handle(ExecutionContext ctx) {
                cb.apply(ctx.req(), ctx.resp());
                return null;
            }

        };
    }

    /**
     * <p>0输入，0输出，一般作为测试</p>
     *
     * @param cb
     * @return
     */
    public static Action fly(Callback.C0 cb) {
        return new Action() {

            @Override
            public ExecutionContext handle(ExecutionContext ctx) {
                cb.apply();
                return null;
            }

        };
    }

    public static Action view(String path, Map map) {
        return fly(() -> Interrupt.render(Renderable.view(path, map)));
    }

//    public static Action file(String s) {
//        return fly(()->render(Renderable.file(s)));
//    }
}
