package com.shuimin.jtiny.core.mw.router;

import com.shuimin.base.S;
import com.shuimin.jtiny.core.ExecutionContext;
import com.shuimin.jtiny.core.Middleware;
import com.shuimin.jtiny.core.http.HttpMethod;
import com.shuimin.jtiny.core.mw.RouteNode;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author ed
 */
public interface Router {

    Middleware route(ExecutionContext ctx);

    Router add(int methodMask, String pattern, Middleware... mw);

    public static class RegexRouter implements Router {

        @Override
        public Middleware route(ExecutionContext ctx) {
            HttpMethod method = HttpMethod.of(ctx.req().method());

            List<RouteNode> routes = Routes.get(method);

            for (RouteNode node : routes) {
                if (node.mather().match(ctx.req())) {
                    return node;
                }
            }
            //not found
            return null;
            //not so hurry to throw an exception
//            throw new HttpException(404, "request " + req.toString() + "not found");
        }

        @Override
        public Router add(int methodMask, String path, Middleware... wares) {
            List<HttpMethod> methods = HttpMethod.unMask(methodMask);
            for (HttpMethod m : methods) {
                List<RouteNode> routes = Routes.get(m);
                S._assert(routes, "routes of method[" + methods.toString() + "] not found");
                routes.add(RouteNode.regexRouteNode(path, Middleware.string(Arrays.asList(wares))));
            }
            return this;
        }

        private static class Routes {
            @SuppressWarnings("unchecked")
            final private static List<RouteNode>[] all = new List[HttpMethod.values().length];

            static List<RouteNode> get(HttpMethod method) {
                List<RouteNode> ret = all[method.ordinal()];
                if (ret == null) {
                    ret = (all[method.ordinal()] = new LinkedList<>());
                }
                return ret;
            }
        }

    }

    public static Router regex() {
        return new RegexRouter();
    }
    //public static Router regex = new TreeRouter();//TODO
}
