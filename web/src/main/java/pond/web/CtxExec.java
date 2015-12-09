package pond.web;

import pond.common.S;
import pond.core.ExecutionContext;
import pond.core.Executor;
import pond.core.Service;


public class CtxExec extends Executor {

  CtxExec() {}

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

  static Service Mid_to_Service(Mid mid){

    Service ret = new Service( _ctx -> {

      Ctx ctx = (Ctx) _ctx;

      if (mid == null) {
        return;
      }

      try {
        if (ctx.handled) return;
        final Mid finalMid = mid;

        S._debug(Pond.logger, log ->
            log.debug("Ctx Executing... uri: " + ctx.req.path() + ", mid: " + finalMid.toString()));

        mid.apply(ctx.req, ctx.resp);
        ctx.handledMids.add(mid);

      } catch (RuntimeException e) {
        unwrapRuntimeException(e, ctx.resp);
      } catch (Exception e) {
        Pond.logger.error("Internal Error", e);
        ctx.resp.send(500, e.getMessage());
      }

    });

    ret.name(mid.toString());
    return ret;
  }

  public void execAll(Ctx ctx, Mid... mids) {
    for (Mid mid : mids) {
      if (ctx.handled) return;
      exec(ctx, Mid_to_Service(mid));
    }
  }

  public void execAll(Ctx ctx, Iterable<Mid> mids) {
    for (Mid mid : mids) {
      if (ctx.handled) return;
      exec(ctx, Mid_to_Service(mid));
    }
  }

  @Override
  public ExecutionContext exec(ExecutionContext context, Service... services) {
//    S._assert(context instanceof Ctx);
    Ctx ctx = (Ctx) context;
    return super.exec(ctx, services);
  }

}
