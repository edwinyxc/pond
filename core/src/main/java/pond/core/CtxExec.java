package pond.core;

import pond.common.f.Callback;
import pond.core.exception.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class CtxExec {

    static Logger logger = LoggerFactory.getLogger(CtxExec.class);
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
        if (t instanceof HttpException) {
            resp.send(((HttpException) t).code(), t.getMessage());
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
            ctxThreadLocal.set(ctx);
            if (ctx.handled) return;
            if (mid != null) {
                logger.debug("Found uri: " + ctx.req.path() + ", mid: " + mid.toString());
                mid.apply(ctx.req, ctx.resp, () -> exec(ctx));
            } else logger.debug("Reach the end of mids");
            //return ctx.isHandled;
        } catch (RuntimeException e) {
            unwrapRuntimeException(e, ctx.resp);
        } catch (Throwable e) {
            e.printStackTrace();
            ctx.resp.send(500, e.getMessage());
        } finally {
            ctxThreadLocal.remove();
        }
        //return false;
    }
}
