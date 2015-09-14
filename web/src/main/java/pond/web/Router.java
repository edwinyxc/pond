package pond.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.S;
import pond.web.http.HttpMethod;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static pond.common.S._assert;
import static pond.common.S._for;


/**
 *
 */
public class Router implements Mid, RouterAPI {

  static Logger logger = LoggerFactory.getLogger(Router.class);
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
  public void apply(Request req, Response resp) {

    Ctx ctx = req.ctx();

    HttpMethod method = HttpMethod.of(req.method());

    ctx.method = method;

    List<Route> routes = this.routes.get(method);


    //ignore trialling slash
    String path = Pond._ignoreLastSlash(req.path());

    S._debug(logger, log -> log.debug("Routing path:" + path));

    long s = S.now();

    List<Route> results = _for(routes).filter(r -> r.match(path)).toList();

    S._debug(logger, log -> {
      log.debug("Routing time: " + (S.now() - s) + "ms");
      if (results.size() == 0)
        logger.debug("Found nothing");
      else
        logger.debug("Found " + results.size() + " Routes:" + results.toString());
    });

    _for(results).each(r -> {

      S._debug(logger, log ->
          log.debug(String.format("Processing... %s", r)));
      //put in-url params
      _for(r.urlParams(path)).each(
          e -> req.param(e.getKey(), e.getValue())
      );

      ctx.route = r;

      ctx.pond.ctxExec.execAll(ctx, r.mids);

      S._debug(logger, log ->
          log.debug(String.format("Process %s finished", r)));

    });

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
      S._debug(logger, log -> log.debug("Routing " + m + " : " + route));
      routes.add(route);
    }
    return this;
  }

  public void clean() {
    routes = new Routes();
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
