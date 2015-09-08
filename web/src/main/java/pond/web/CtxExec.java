package pond.web;

import pond.common.S;


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
      Pond.logger.error(e.getMessage(), e);
      resp.send(500, e.getMessage());
      return;
    }
    if (t instanceof RuntimeException) {
      unwrapRuntimeException((RuntimeException) t, resp);
    } else {
      Pond.logger.error(t.getMessage(), t);
      resp.send(500, t.getMessage());
    }
  }

  public void execAll(Ctx ctx, Mid... mids) {
    for (Mid mid : mids) {
      if (ctx.handled) return;
      exec(ctx, mid);
    }
  }

  public void execAll(Ctx ctx, Iterable<Mid> mids) {
    for (Mid mid : mids) {
      if (ctx.handled) return;
      exec(ctx, mid);
    }
  }
  /**
   * @param ctx
   */
  public void exec(Ctx ctx, Mid mid) {

    if (mid == null) {
      return;
    }

    try {
      //bind localthread-context
      if (ctxThreadLocal.get() == null) ctxThreadLocal.set(ctx);
      if (ctx.handled) return;

        final Mid finalMid = mid;

        S._debug(Pond.logger, log ->
            log.debug("Ctx Executing... uri: " + ctx.req.path() + ", mid: " + finalMid.toString()));

        mid.apply(ctx.req, ctx.resp);

    } catch (RuntimeException e) {
      unwrapRuntimeException(e, ctx.resp);
    } catch (Exception e) {
      Pond.logger.error("Internal Error", e);
      ctx.resp.send(500, e.getMessage());
    } finally {
      Ctx thctx;
      if ((thctx = ctxThreadLocal.get()) != null) {
        S._debug(Pond.logger, logger ->
            logger.debug("Ctx costs: " + (S.now() - (long) thctx.get("_start_time")) + "ms"));
        ctxThreadLocal.remove();
      }
    }
  }
}
