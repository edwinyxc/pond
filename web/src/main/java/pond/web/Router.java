package pond.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.S;
import pond.common.STRING;
import pond.web.http.HttpMethod;

import java.util.LinkedList;
import java.util.List;

import static pond.common.S.*;


/**
 *
 */
public class Router implements Mid, RouterAPI {

  static Logger logger = LoggerFactory.getLogger(Router.class);
  Routes routes = new Routes();
  protected String prefix = "";

  public Router prefix(String prefix) {
    _debug(logger, log -> log.debug(prefix));
    this.prefix = prefix;
    return this;
  }

  @Override
  public void apply(Request req, Response resp) {

    Ctx ctx = req.ctx();

    HttpMethod method = HttpMethod.of(req.method());

    ctx.method = method;

    List<Route> routes = this.routes.get(method);


    //ignore trialling slash
    String path = req.path();//Pond._ignoreLastSlash(req.path());

    int indexOfLastSlash;
    if (STRING.notBlank(prefix) && (indexOfLastSlash = prefix.lastIndexOf("/")) != -1) {
      path = path.substring(indexOfLastSlash);
//      _debug(logger, log -> log.debug("Prefix path:" + path));
    }

    String finalPath = path;
    _debug(logger, log -> log.debug("Routing path:" + finalPath));

    long s = S.now();

    List<Route> results = _for(routes).filter(r -> r.match(finalPath)).toList();

    _debug(logger, log -> {
      log.debug("Routing time: " + (S.now() - s) + "ms");
      if (results.size() == 0)
        logger.debug("Found nothing");
      else
        logger.debug("Found " + results.size() + " Routes:" + results.toString());
    });

    _for(results).each(r -> {

      _debug(logger, log ->
          log.debug(String.format("Processing... %s", r)));
      //put in-url params
      _for(r.urlParams(finalPath)).each(
          e -> req.param(e.getKey(), e.getValue())
      );

      ctx.route = r;

      ctx.pond.ctxExec.execAll(ctx, r.mids);

      _debug(logger, log ->
          log.debug(String.format("Process %s finished", r)));

    });

  }

  @Override
  public Router use(int methodMask, String defPath, Mid... mids) {
    List<HttpMethod> methods = HttpMethod.unMask(methodMask);
    for (HttpMethod m : methods) {

      List<Route> routes = this.routes.get(m);
      _assert(routes, "Routes of method[" + methods.toString() + "] not found");

      List<Mid> middles = S.array(mids).map(mid -> {
        if (mid instanceof Router) {
          return ((Router) mid).prefix(defPath);
        }
        return mid;
      }).toList();

      Route route = new Route(defPath, middles);

      _debug(logger, log -> log.debug("Routing " + m + " : " + route));
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
