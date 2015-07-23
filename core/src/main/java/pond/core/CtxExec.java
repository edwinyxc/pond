package pond.core;

import pond.common.S;

import java.util.Collections;
import java.util.List;


public class CtxExec {

    private static ThreadLocal<Ctx> ctxThreadLocal = new ThreadLocal<>();

    public static Ctx get() {
        return ctxThreadLocal.get();
    }

    public CtxExec() {
    }

    static void unwrapRuntimeException(RuntimeException e, Response resp) {
        Throwable t = e.getCause();
        if (t == null) {
            e.printStackTrace();
            resp.send(500, e.getMessage());
            return;
        }
        if (t instanceof RuntimeException) {
            unwrapRuntimeException((RuntimeException) t, resp);
        } else {
            t.printStackTrace();
            resp.send(500, t.getMessage());
        }
    }

    public void exec(Ctx ctx) {
        exec(ctx, Collections.emptyList());
    }

    /**
     * run a ctx
     *
     * @param ctx
     */
    public void exec(Ctx ctx, List<Mid> additionalMids) {

        Mid mid = ctx.getMid();
        ctx.addMids(additionalMids);
        if (mid == null) {
            mid = ctx.getMid();
        }
        try {
            //bind localthread-context
            if (ctxThreadLocal.get() == null) ctxThreadLocal.set(ctx);
            if (ctx.handled) return;
            if (mid != null) {
                final Mid finalMid = mid;
                S._debug(Pond.logger, log -> log.debug("Found uri: "
                        + ctx.req.path() + ", mid: " + finalMid.toString()));
                mid.apply(ctx.req, ctx.resp);
                exec(ctx);
            }
            //reach the end of mids
            if (!ctx.handled)
                ctx.resp.send(404);
        } catch (RuntimeException e) {
            unwrapRuntimeException(e, ctx.resp);
        } catch (Throwable e) {
            e.printStackTrace();
            ctx.resp.send(500, e.getMessage());
        } finally {
            Ctx thctx;
            if ((thctx = ctxThreadLocal.get()) != null) {
                S._debug(Pond.logger, logger -> {
                    logger.debug("Ctx costs: " + (S.now() - (long) thctx.get("_start_time")) + "ms");
                });
                ctxThreadLocal.remove();
            }
        }
        //return false;
    }
}
