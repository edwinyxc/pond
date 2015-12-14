package pond.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.S;
import pond.common.SPILoader;
import pond.web.http.HttpMethod;
import pond.web.spi.PathToRegCompiler;

import java.util.*;
import java.util.regex.Pattern;

import static pond.common.S.*;


public class Router implements Mid, RouterAPI {
  /*
  TODO: add config for caseSensitive, mergeParams, strict
   */
  static Logger logger = LoggerFactory.getLogger(Router.class);
  Routes routes = new Routes();

  List<Mid> defaultMids = new ArrayList<>();

  private String get_path_remainder(Request req){

    String path = req.path();
    Ctx ctx = req.ctx();

    Route entry_route = ctx.route;

    PathToRegCompiler compiler = SPILoader.service(PathToRegCompiler.class);

    return compiler.preparePath(entry_route, path);
  }

  @Override
  public void apply(Request req, Response resp) {

    Ctx ctx = req.ctx();

    HttpMethod method = HttpMethod.of(req.method());

    ctx.method = method;

    List<Route> routes = this.routes.get(method);

    String path = get_path_remainder(req);

    _debug(logger, log -> log.debug("Routing path:" + path));

    long s = S.now();

    RegPathMatchResult matchResult;
    for(Route r : routes){
      //jump out
      if(ctx.handled)break;

      matchResult = r.match(path);
      if(matchResult.matches){
        _debug(logger, log -> {
          log.debug("Routing time: " + (S.now() - s) + "ms");
          log.debug(String.format("Processing... %s", r));
        });

        //put in-url params
        _for(matchResult.params.entrySet()).each(
            e -> req.param(e.getKey(), e.getValue())
        );

        ctx.route = r;
        ctx.pond.ctxExec.execAll(ctx, r.mids());

        _debug(logger, log ->
            log.debug(String.format("Process %s finished", r)));
      }
    }

    if (!ctx.handled) {
      _debug(logger, log ->
          log.debug("Found nothing, executing default Middlewares"));
      ctx.pond.ctxExec.execAll(ctx, defaultMids);
    }
  }

  @Override
  public Router use(int mask, Pattern path, String[] inUrlParams, Mid[] mids) {

    List<HttpMethod> methods = HttpMethod.unMask(mask);

    for (HttpMethod m : methods) {

      List<Route> routes = this.routes.get(m);

      List<Mid> middles = S.array(mids);

      Route route = new Route(path, inUrlParams, middles);

      _debug(logger, log -> log.debug("Routing " + m + " : " + route));

      routes.add(route);
    }
    return this;
  }

  @Override
  public Router otherwise(Mid... mids) {
    defaultMids.addAll(Arrays.asList(mids));
    return this;
  }

  public void clean() {
    routes = new Routes();
  }

  @SuppressWarnings("unchecked")
  private static class Routes {

    final private List<Route>[] all = new List[HttpMethod.values().length];

    List<Route> get(HttpMethod method) {
      List ret = all[method.ordinal()];
      if (ret == null) {
        ret = (all[method.ordinal()] = new LinkedList<>());
      }
      return (List<Route>) ret;
    }
  }

}
