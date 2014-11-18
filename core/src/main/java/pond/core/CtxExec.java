package pond.core;

import pond.common.f.Callback;
import pond.core.exception.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;


public class CtxExec {

    static Logger logger = LoggerFactory.getLogger(CtxExec.class);
    private static ThreadLocal<Ctx> ctxThreadLocal = new ThreadLocal<Ctx>();

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

    /**
     * run a ctx
     *
     * @param ctx
     */
    public boolean exec(Ctx ctx, List<Mid> mids) {
        Callback.C3<Request, Response, Callback.C0> mid = ctx.nextMid();
        ctx.addMid(mids);
        try {
            ctxThreadLocal.set(ctx);
            if (mid != null) {
                logger.info("uri=" + ctx.req.path() + ",mid=" + mid.toString());
                mid.apply(ctx.req, ctx.resp,
                        () -> exec(ctx, Collections.<Mid>emptyList()));
            }
            return ctx.isHandled;
        } catch (RuntimeException e) {
            unwrapRuntimeException(e, ctx.resp);
        } catch (Throwable e) {
            e.printStackTrace();
            ctx.resp.send(500, e.getMessage());
//            throw new RuntimeException(e);
        } finally {
            ctxThreadLocal.remove();
        }
        return false;
    }
}
