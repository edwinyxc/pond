package com.shuimin.pond.core.router;

import com.shuimin.common.S;
import com.shuimin.common.f.Callback;
import com.shuimin.pond.core.*;
import com.shuimin.pond.core.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.shuimin.common.S._assert;
import static com.shuimin.common.S._for;

/**
 *
 */
public class Router implements Mid, RouterAPI {

    static Logger logger = LoggerFactory.getLogger(Ctx.class);
    Routes routes = new Routes();
    protected String prefix = "";

    public Router prefix(String prefix) {
        this.prefix = prefix;
        for (HttpMethod m : HttpMethod.values()) {
            List<Route> routes = this.routes.get(m);
            for (Route r : routes) {
                r.prefix(this.prefix);
                logger.debug("Add prefix " + prefix + " :" + r.toString());
            }
        }
        return this;
    }

    @Override
    public void apply(Request req, Response resp, Callback.C0 next) {

        HttpMethod method = HttpMethod.of(req.method());

        List<Route> routes = this.routes.get(method);

        //ignore trialling slash
        String path = Pond._ignoreLastSlash(req.path());

        logger.debug("Routing path:" + path);
        long s = S.time();
//        List<Route> result = new LinkedList<>();
//        for (Route node : routes) {
//            if (node.match(path)) {
//                result.add(node);
//            }
//        }
        // 通配获得最小为优先, * 通配符优先级最低
        Iterable<Route> resultList = _for(routes)
                        .filter(r -> r.match(path)).val();

        Route route_f = _for(resultList)
                        .reduce((r, r1) -> {
                            if (r.def_path.contains("*")) return r1;
                            if (r1.def_path.contains("*")) return r;
                            return
                            r.def.matcher(path).groupCount()
                                    <= r1.def.matcher(path).groupCount() ?
                                    r : r1;
                        });

        logger.debug("Routing time: " + (S.time() - s) + "ms");
        if (route_f == null)
            logger.debug("Found nothing");
        else {
            logger.debug("Found Route:" + route_f.toString());

            //put in-url params
            _for(route_f.urlParams(path)).each(
                    e -> req.param(e.getKey(), e.getValue())
            );
            req.ctx().put("route", route_f);
            // trigger CtxExec
            CtxExec.exec(req.ctx(), route_f.mids);
        }

//            for (Route r : result) {
//                logger.debug("Found Route:" + r.toString());
//                //put in-url params
//                _for(r.urlParams(path)).each(
//                        e -> req.param(e.getKey(), e.getValue())
//                );
//                req.ctx().put("route", r);
//                // trigger CtxExec
//                CtxExec.exec(req.ctx(), r.mids);
//                //FIXME
//                // When i try to string all the result-mids as stack,
//                //it seems it`s impossible to re-bind request value
//                //( either binding url param or removing binding)
//                //So, easiest way is only dispatch to the first
//                //available Mid
//                break;
//            }
//    }
        //why?
        next.apply();
    }

    @Override
    public Router use(String path, Router router) {
        router.prefix(path);
        for (HttpMethod m : HttpMethod.values()) {
            //add to this router
            List<Route> routeList = this.routes.get(m);
            routeList.addAll(router.routes.get(m));
        }
        return this;
    }

    @Override
    public Router use(int methodMask, String path, Mid... mids) {
        List<HttpMethod> methods = HttpMethod.unMask(methodMask);

        for (HttpMethod m : methods) {
            List<Route> routes = this.routes.get(m);
            _assert(routes,
                    "Routes of method[" + methods.toString() + "] not found");
            //prefix :  /${id}
            Route route = new Route(path, Arrays.asList(mids));
            logger.debug(S.dump(route));
            routes.add(route);
        }
        return this;
    }

    private static class Routes {
        @SuppressWarnings("unchecked")
        final private List<Route>[] all = new List[HttpMethod.values().length];

        List<Route> get(HttpMethod method) {
            List<Route> ret = all[method.ordinal()];
            if (ret == null) {
                ret = (all[method.ordinal()] = new LinkedList<>());
            }
            return ret;
        }
    }

}
