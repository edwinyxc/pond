package pond.web;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.S;
import pond.common.f.Callback;
import pond.common.f.Function;
import pond.core.Context;
import pond.core.Service;
import pond.core.Services;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Created by ed on 21/02/17.
 */
public class Ctx extends Context {
  public static Logger logger = LoggerFactory.getLogger(Ctx.class);

  public final String method;
  public final String uri;
  public final String path;
  final Object raw;
  public final ChannelHandlerContext context;
  final Map<String, List<String>> inUrlParams = new HashMap<>();

  boolean handled = false;
  List<CtxHandler> handledCtxCallbacks = new LinkedList<>();
  Route route;

  public Ctx updateInUrlParams(Callback<Map<String, List<String>>> b) {
    b.apply(inUrlParams);
    return this;
  }

  static {
    Function<Service, CtxHandler> ctx_handler_to_service = ctxHandler -> {

      Service ret = new Service(_ctx -> {
        Ctx ctx = (Ctx) _ctx;
        if (ctxHandler == null) return;
        try {
          if (ctx.handled) return;
          final CtxHandler finalCtxHandler = ctxHandler;

          S._debug(logger, log ->
              log.debug("Ctx Executing... uri: " + ctx.uri + ", express: " + finalCtxHandler.toString()));

          ctxHandler.apply(ctx);
          ctx.handledCtxCallbacks.add(ctxHandler);

        } catch (RuntimeException e) {
          ctx.unwrapRuntimeException(e);
        } catch (Exception e) {
          logger.error("Internal Error", e);
          ctx.send(e.getMessage());
        }
      });

      ret.name(ctxHandler.toString());
      return ret;
    };

    Services.adapter(CtxHandler.class, ctx_handler_to_service);
  }

  public Ctx(String method, String uri, Object raw, ChannelHandlerContext channelHandlerContext) {
    super("system-web-ctx");
    this.method = method;
    this.uri = uri;
    this.raw = raw;
    this.path = S._try_ret(() -> new URI(uri).getPath());
    this.context = channelHandlerContext;
    S._debug(logger, log -> {
      log.debug("Main ctx route:");
      super.set("_start_time", S.now());
      log.debug("ctx starts at: " + this.get("_start_time"));
      log.debug("method:"+method);
      log.debug("uri:"+uri);
      log.debug("path:"+path);
    });
  }

  public void send(String msg) {
    this.context.writeAndFlush(msg);
  }

  public void unwrapRuntimeException(RuntimeException e) {

    if (e instanceof EndToEndException) {
      EndToEndException ete = (EndToEndException)e;
      logger.warn("EEE:"
                      + ((EndToEndException) e).http_status
                      + ":" + e.getMessage(), e);
      if(this instanceof HttpCtx)
        ((HttpCtx)this).resp.sendError(ete.http_status, ete.message);
      else
        send(e.getMessage());
      return;
    }

    Throwable t = e.getCause();
    if (t == null) {
      logger.error(e.getMessage(), e);
      if(this instanceof HttpCtx)
        ((HttpCtx)this).resp.sendError(500, e.getMessage());
      else
        send(e.getMessage());
      return;
    }

    if (t instanceof RuntimeException) {
      unwrapRuntimeException((RuntimeException) t);
    } else {
      logger.error(t.getMessage(), t);
      if(this instanceof HttpCtx)
        ((HttpCtx)this).resp.sendError(500, e.getMessage());
      else
        send(e.getMessage());
    }
  }


  public Route route() {
    return route;
  }

  public void execAll(CtxHandler... mids) {
    for (CtxHandler mid : mids) {
      if (handled) return;
      this.exec(mid);
    }
  }

  public void execAll(Iterable<CtxHandler> mids) {
    for (CtxHandler mid : mids) {
      if (handled) return;
      this.exec(mid);
    }
  }

  public <E extends Ctx> E setHandled() {
    this.handled = true;
    return (E) this;
  }

  public Object rawMsg() {
    return this.raw;
  }

  public void put(String name, Object o) {
    super.set(name, o);
  }

  public boolean isHandled(CtxHandler mid) {
    return handledCtxCallbacks.contains(mid);
  }

  public <T> void result(ResultDef<T> resultDef, T value){
    resultDef.apply(this, value);
  }

  public void result(ResultDef<Void> resultDef){
    resultDef.apply(this, null);
  }

  public List<CtxHandler> handledCtxCalbacks() {
    return handledCtxCallbacks;
  }

}
