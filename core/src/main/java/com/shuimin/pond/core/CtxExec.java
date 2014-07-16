package com.shuimin.pond.core;

import com.shuimin.common.f.Callback;
import com.shuimin.pond.core.exception.HttpException;

import java.util.Collections;
import java.util.List;


public class CtxExec {

    private static ThreadLocal<Ctx> ctxThreadLocal = new ThreadLocal<Ctx>();

    public static Ctx get() {
        return ctxThreadLocal.get();
    }

    /**
     * run a ctx
     *
     * @param ctx
     */
    public static void exec(Ctx ctx, List<Mid> mids) {
        Callback.C3<Request, Response, Callback.C0> mid = ctx.nextMid();
        ctx.addMid(mids);
        try {
            ctxThreadLocal.set(ctx);
            if (mid != null) {
                System.out.println("uri="+ctx.req.path()+",mid="+mid.toString());
                mid.apply(ctx.req, ctx.resp,
                        () -> exec(ctx, Collections.<Mid>emptyList()));
            }
        } catch (HttpException e) {
            e.printStackTrace();
            ctx.resp.sendError(e.code(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            ctx.resp.sendError(500, e.getMessage());
            throw new RuntimeException(e);
        } finally {
            ctxThreadLocal.remove();
        }
    }
}
