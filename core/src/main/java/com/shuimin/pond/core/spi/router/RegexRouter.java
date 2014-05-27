package com.shuimin.pond.core.spi.router;

import com.shuimin.common.S;
import com.shuimin.common.f.Function;
import com.shuimin.pond.core.ExecutionContext;
import com.shuimin.pond.core.Middleware;
import com.shuimin.pond.core.Request;
import com.shuimin.pond.core.http.HttpMethod;
import com.shuimin.pond.core.spi.Router;

import java.util.LinkedList;
import java.util.List;

import static com.shuimin.common.S._for;
import static com.shuimin.pond.core.Pond.debug;

/**
 * Created by ed on 5/26/14.
 */
public class RegexRouter implements Router {
    Routes routes = new Routes();

    @Override
    public Middleware route(ExecutionContext ctx) {
        HttpMethod method = HttpMethod.of(ctx.req().method());

        List<RouteNode> routes = this.routes.get(method);

        for (RouteNode node : routes) {
            if (node.match(ctx.req())) {
                return node;
            }
        }
        //not found
        return null;
    }

    @Override
    public Router add(int methodMask, String path, Middleware... wares) {
        List<HttpMethod> methods = HttpMethod.unMask(methodMask);
        for (HttpMethod m : methods) {
            List<RouteNode> routes = this.routes.get(m);
            S._assert(routes, "routes of method[" + methods.toString() + "] not found");
            routes.add(RouteNode.of(path,
                    Middleware.string(
                            S.list.one(_for(wares).each(Middleware::init).val()))
            ));
            debug("Route{" + m.toString() + "," + path + "," + wares.toString() + "}");
        }
        return this;
    }


    private static class Routes {
        @SuppressWarnings("unchecked")
        final private List<RouteNode>[] all = new List[HttpMethod.values().length];

        List<RouteNode> get(HttpMethod method) {
            List<RouteNode> ret = all[method.ordinal()];
            if (ret == null) {
                ret = (all[method.ordinal()] = new LinkedList<>());
            }
            return ret;
        }
    }

}
