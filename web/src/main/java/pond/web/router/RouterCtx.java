package pond.web.router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.S;
import pond.common.f.Callback;
import pond.core.CtxHandler;
import pond.core.Entry;
import pond.web.http.HttpCtx;

import java.util.List;
import java.util.Map;

import static pond.core.CtxHandler.NOOP;

public interface RouterCtx extends HttpCtx {

    Entry<Router> ROUTER = new Entry<>(RouterCtx.class, "ROUTER");
    Entry<Route> ROUTE = new Entry<>(RouterCtx.class, "ROUTE");
    Entry<Long> ROUTING_START_TIME = new Entry<>(RouterCtx.class, "ROUTING_START_TIME");
    Entry<String> PATH_REMINDER = new Entry<>(RouterCtx.class,"PATH_REMINDER");
    Entry<String> PATH = new Entry<>(RouterCtx.class,"PATH");
    Entry<CtxHandler<HttpCtx>> CONTINUE = new Entry<>(RouterCtx.class, "CONTINUE");

    Logger logger = LoggerFactory.getLogger(RouterCtx.class);


//    boolean handled = false;
//    List<CtxHandler> handledCtxCallbacks = new LinkedList<>();
//    Route currentRoute;

    default Route currentRoute() {
        return this.getEntry(ROUTE);
    }

    default String routingPath(){
        return this.getOrPutDefault(PATH, this.path());
    }

    default RouterCtx updateInUrlParams(Callback<Map<String, List<String>>> b) {
        var queries= (HttpCtx.Queries) this::bind;
        b.apply(queries.inUrlParams());
        return this;
    }

    @SuppressWarnings("unchecked")
    default void continueRouting(){
        insert(this.getOrPutDefault(CONTINUE, NOOP));
    }

    default void reRoute(String newPath){
        this.reRoute(this.getEntry(ROUTER), newPath);
    }

    default void reRoute(Router router, String newPath){
        this.set(PATH_REMINDER, null);
        this.set(PATH, newPath);
        this.set(ROUTE, null);
        S.echo("re-routing to" + newPath);
        insert(router);
    }


//    static {
//        Function<Service, CtxHandler> ctx_handler_to_service = ctxHandler -> {
//
//            Service ret = new Service(_ctx -> {
//                Ctx ctx = (Ctx) _ctx;
//                if (ctxHandler == null) return;
//                try {
//                    if (ctx.handled) return;
//                    final CtxHandler finalCtxHandler = ctxHandler;
//
//                    S._debug(logger, log ->
//                            log.debug("Context Executing... uri: " + ctx.uri + ", toCtxHandler: " + finalCtxHandler.toString()));
//
//                    ctxHandler.apply(ctx);
//                    ctx.handledCtxCallbacks.add(ctxHandler);
//
//                } catch (RuntimeException e) {
//                    ctx.unwrapRuntimeException(e);
//                    e.printStackTrace();
//                } catch (Exception e) {
//                    logger.error("Internal Error", e);
//                    ctx.send(e.getMessage());
//                }
//            });
//
//            ret.name(ctxHandler.toString());
//            return ret;
//        };
//
//        //TODO
//        //Services.adapter(CtxHandler.class, ctx_handler_to_service);
//    }

//    public Ctx(String method, String uri, Object bodyAsRaw, ChannelHandlerContext channelHandlerContext) {
//        super("system-web-ctx");
//        this.method = method;
//        this.uri = uri;
//        this.bodyAsRaw = bodyAsRaw;
//        this.path = S._try_ret(() -> new URI(uri).getPath());
//        this.context = channelHandlerContext;
//        S._debug(logger, log -> {
//            log.debug("Main ctx currentRoute:");
//            super.set("_start_time", S.now());
//            log.debug("ctx starts at: " + this.getEntry("_start_time"));
//            log.debug("method:" + method);
//            log.debug("uri:" + uri);
//            log.debug("path:" + path);
//        });
//    }

//    public void send(String msg) {
//        this.context.writeAndFlush(msg);
//    }

//    public void unwrapRuntimeException(RuntimeException e) {
//
//        if (e instanceof EndToEndException) {
//            EndToEndException ete = (EndToEndException) e;
//            logger.warn("EEE:"
//                    + ((EndToEndException) e).http_status
//                    + ":" + e.getMessage(), e);
//            if (this instanceof HttpCtx)
//                ((HttpCtx) this).resp.sendError(ete.http_status, ete.message);
//            else
//                send(e.getMessage());
//            return;
//        }
//
//        Throwable t = e.getCause();
//        if (t == null) {
//            logger.error(e.getMessage(), e);
//            if (this instanceof HttpCtx)
//                ((HttpCtx) this).resp.sendError(500, e.getMessage());
//            else
//                send(e.getMessage());
//            return;
//        }
//
//        if (t instanceof RuntimeException) {
//            unwrapRuntimeException((RuntimeException) t);
//        } else {
//            logger.error(t.getMessage(), t);
//            if (this instanceof HttpCtx)
//                ((HttpCtx) this).resp.sendError(500, e.getMessage());
//            else
//                send(e.getMessage());
//        }
//    }



}
