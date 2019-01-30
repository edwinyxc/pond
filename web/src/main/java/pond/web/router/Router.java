package pond.web.router;

import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.S;
import pond.common.f.Callback;
import pond.core.Ctx;
import pond.core.CtxHandler;
import pond.web.http.HttpCtx;


import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import static pond.common.S._debug;
import static pond.common.S._for;
import static pond.web.http.HttpCtx.appendToMap;


public class Router implements CtxHandler<HttpCtx>, RouterAPI {

    /*
    TODO: add config for caseSensitive, mergeParams, strict
    TODO: MIME base routing consume Content-Type / provide Accpet
     */
    static Logger logger = LoggerFactory.getLogger(Router.class);
    static PathToRegCompiler compiler = new ExpressPathToRegCompiler();

    public Router(){ }
    public Router(Callback<Router> config){
        config.apply(this);
    }

    private Routes routes = new Routes();
    protected Router parent = null;
    final protected List<Router> children = new LinkedList<>();
    protected String basePath = "/";

    private final List<CtxHandler> otherness = List.of(
        //404 not found
        (CtxHandler.consume(Ctx.SELF, ctx -> {
            var s = (HttpCtx.Send)ctx::bind;
            s.sendNotFound(Unpooled.wrappedBuffer("Router not Found Anything".getBytes()));
        }))
    );

    public Routes routes() {
        return routes;
    }


    private String pathRemainder(RouterCtx ctx) {
        String path = S.avoidNull(ctx.getEntry(RouterCtx.PATH_REMINDER), ctx.routingPath());
        S.echo("path_reminder", path);
        Route entry_route = ctx.currentRoute();
        return compiler.preparePath(entry_route, path);
    }

    private void insertRoute(HttpCtx ctx, Route r){
        ctx.set(RouterCtx.ROUTER, this);
        ctx.set(RouterCtx.ROUTE, r);
        var handlers = r.handlers();
        //build the continue-function
        CtxHandler<HttpCtx> continueFunc = this;
        ctx.set(RouterCtx.CONTINUE, continueFunc);
        //send to
        S._for(handlers).reverse().map(handler -> {
            if(handler instanceof CtxHandler.Flow){
                return handler;
            }else {
                return handler.flowTo(ctx.flowProcessor());
            }
        }).each(ctx::insert);
    }


    @Override
    public void apply(HttpCtx http) {

        //rebind
        RouterCtx ctx = http::bind;

        HttpMethod method = HttpMethod.of(ctx.method());

        List<Route> routes = this.routes.get(method.ordinal());

        String path = pathRemainder(ctx);

        ctx.set(RouterCtx.PATH_REMINDER, path);

        _debug(logger, log -> log.debug("Routing path:" + path));

        long s = S.now();

        RegPathMatchResult matchResult;
        for (Route r : routes) {
            var lastRoute = ctx.getEntry(RouterCtx.ROUTE);
            if(lastRoute != null && lastRoute == r) continue;

            //jump out
            //if (ctx.handled) break;

            matchResult = r.match(path);
            if (matchResult.matches) {
                _debug(logger, log -> {
                    log.debug("Routing time: " + (S.now() - s) + "ms");
                    log.debug(String.format("Processing... %s", r));
                });

                //put in-url queries
                _for(matchResult.params.entrySet()).each(
                        e -> ctx.updateInUrlParams(params -> {
                            appendToMap(params, e.getKey(), e.getValue());
                        })
                );

                insertRoute(ctx, r);
                return;
//                _debug(logger, log ->
//                        log.debug(String.format("Process %s finished", r)));
            }
        }

        //end
        logger.debug("Found nothing, executing defaults: " + S.dump(otherness));

        //execute defaults
        S._for(otherness).reverse().each(ctx::insert);
    }

    protected String sanitizedBasePath(String basePath) {
        //format /:xxx/:aaa/:bbb --> /{xxx}/{aaa}/{bbb}
        String[] words = basePath.split("/");
        String parsed = String.join("/", S._for(words).map(w ->
                w.startsWith(":")
                        ? "{" + w.substring(1) + "}"
                        : w
        ));
        int idx_last_asterisk = parsed.lastIndexOf('*');
        if (idx_last_asterisk > 0) {
            return parsed.substring(0, idx_last_asterisk);
        }
        return parsed;
    }

    protected String buildPathForRoute(String base) {
        var newPath = RouterAPI.sanitisePath(this.basePath, base);
        S.echo("sanitised", newPath);
        newPath = newPath.replaceAll(":(\\w+)", "{$1}");
        newPath = newPath.replaceAll("/\\*$", "");
        if(newPath.length() == 0) newPath = "/";
        S.echo("replaced", newPath);
        return newPath;
    }
//
//    void setBasePath(String base){
//        String saned = buildPathForRoute(base);
//        this.basePath = saned;
//        S._for(children).each(child -> {
//            child.setBasePath(saned);
////            child.basePath = child.basePath + absPath;
//        });
//    }

    protected String absolutePath(){
        String ret = basePath;
        if(parent != null){
            String parentPath = parent.absolutePath();
            if(parentPath.endsWith("/")){
                parentPath = parentPath.substring(0, parentPath.length()-1);
            }
            ret = parentPath + ret;
        }
        return ret;
    }

    @Override
    public void configRoute(Route route, CtxHandler use) {
        if (use instanceof Router) {
            Router child = ((Router) use);
            this.children.add(child);
//            child.setBasePath(currentRoute.basePath());
            child.parent = this;
            child.basePath = buildPathForRoute(route.basePath());
        }
    }



    @Override
    public Router use(int mask, Pattern path, String pathDef, String[] inUrlParams, CtxHandler[] mids) {

        List<HttpMethod> methods = HttpMethod.unMask(mask);

//    this.pathDef = pathDef;

        for (HttpMethod m : methods) {

            List<Route> routes = this.routes.get(m.ordinal());

            List<CtxHandler> middles = S.array(mids).toList();
            //build currentRoute
            Route route = new Route(m, path, inUrlParams, pathDef, middles);

            //do the config
            for (CtxHandler use : mids) {
                configRoute(route, use);
            }

            String pattern = path.pattern();

            int found = -1;
            String existingPattern = null;

            for (int i = 0; i < routes.size(); i++) {
                existingPattern = routes.get(i).defPath().pattern();
                if (existingPattern.equals(pattern)) {
                    found = i;
                    break;
                }
            }

            if (found >= 0) {
                logger.warn("Override existing router: "
                        + existingPattern
                        + " with " + route);

                routes.remove(found);
                routes.add(found, route);

            } else {
                _debug(logger, log -> log.debug("Add new Route" + m + " : " + route));
                routes.add(route);
            }

        }

        return this;
    }

    @Override
    public Router otherwise(CtxHandler... mids) {
        otherness.addAll(Arrays.asList(mids));
        return this;
    }

    public void clean() {
        routes = new Routes();
    }


    @SuppressWarnings("unchecked")
    public static class Routes {

        final private List<Route>[] all = new List[HttpMethod.values().length];

        public List<Route> get(int httpMethodOrdinal) {
            List ret = all[httpMethodOrdinal];
            if (ret == null) {
                ret = (all[httpMethodOrdinal] = new LinkedList<>());
            }
            return (List<Route>) ret;
        }
    }

}
