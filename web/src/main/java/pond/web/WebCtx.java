package pond.web;

import pond.common.S;
import pond.common.f.Function;
import pond.core.Context;
import pond.core.Service;
import pond.core.Services;
import pond.web.http.HttpMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Execution Context, attached to a single thread.
 */
public class WebCtx extends Context {

  static {}

  final Request req;
  final Response resp;
  Pond pond;
  //original path
  final String path;
  final String uri;
  Route route;
  HttpMethod method;
  List<Mid> handledMids = new ArrayList<>();
  boolean handled = false;

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

  static {
    Function<Service, Mid> mid_to_service = mid -> {

      Service ret = new Service(_ctx -> {
        WebCtx ctx = (WebCtx) _ctx;
        if (mid == null) return;
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
    };

    Services.adapter(Mid.class, mid_to_service);
  }


  public WebCtx(Request req, Response resp, Pond pond) {
    super("system");
    this.resp = new ResponseWrapper(this, resp);
    req = (this.req = new RequestWrapper(this, req));
    this.path = req.path();
    this.uri = req.uri();
    this.pond = pond;

    S._debug(Pond.logger, log -> {
      log.debug("Main ctx route:");
      super.set("_start_time", S.now());
      log.debug("ctx starts at: " + this.get("_start_time"));

    });
  }

  public void execAll(Mid... mids) {
    for (Mid mid : mids) {
      if (handled) return;
      this.exec(mid);
    }
  }

  public void execAll(Iterable<Mid> mids) {
    for (Mid mid : mids) {
      if (handled) return;
      this.exec(mid);
    }
  }

  public void put(String name, Object o) {
    super.set(name, o);
  }

  public boolean isHandled(Mid mid) {
    return handledMids.contains(mid);
  }

  public List<Mid> handledMids() {
    return handledMids;
  }

  public Pond pond() {
    return pond;
  }

  public Route route() {
    return route;
  }

  public WebCtx route(Route r) {
    route = r;
    return this;
  }

  void setHandled(boolean b) {
    this.handled = b;
  }

  public Request req() {
    return req;
  }

  public Response resp() {
    return resp;
  }

  public String uri() {
    return uri;
  }

  public String path() {
    return path;
  }

  public HttpMethod method() {
    return method;
  }

}
