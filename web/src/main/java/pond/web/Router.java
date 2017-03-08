package pond.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.S;
import pond.web.http.HttpMethod;
import pond.web.http.HttpUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import static pond.common.S._debug;
import static pond.common.S._for;


public class Router implements CtxHandler, RouterAPI {
  /*
  TODO: add config for caseSensitive, mergeParams, strict
   */
  static Logger logger = LoggerFactory.getLogger(Router.class);
  static PathToRegCompiler compiler = new ExpressPathToRegCompiler();
  Routes routes = new Routes();

  List<CtxHandler> defaultMids = new ArrayList<>();

  private String get_path_remainder(Ctx ctx) {

    String path = S.avoidNull((String) ctx.get("last_remainder"), ctx.path);

    Route entry_route = ctx.route;
    return compiler.preparePath(entry_route, path);
  }

  @Override
  public void apply(Ctx ctx) {

    HttpMethod method = HttpMethod.of(ctx.method);

    List<Route> routes = this.routes.get(method.ordinal());

    String path = get_path_remainder(ctx);
    ctx.set("last_remainder", path);

    _debug(logger, log -> log.debug("Routing path:" + path));

    long s = S.now();

    RegPathMatchResult matchResult;
    for (Route r : routes) {
      //jump out
      if (ctx.handled) break;

      matchResult = r.match(path);
      if (matchResult.matches) {
        _debug(logger, log -> {
          log.debug("Routing time: " + (S.now() - s) + "ms");
          log.debug(String.format("Processing... %s", r));
        });

        //put in-url params
        _for(matchResult.params.entrySet()).each(
            e -> ctx.updateInUrlParams(params -> {
              HttpUtils.appendToMap(params, e.getKey(), e.getValue());
            })
        );

        ctx.route = r;
        ctx.execAll(r.mids());

        _debug(logger, log ->
            log.debug(String.format("Process %s finished", r)));
      }
    }

    if (!ctx.handled) {
      _debug(logger, log ->
          log.debug("Found nothing, executing default Middlewares"));
      ctx.execAll(defaultMids);
    }
  }

  @Override
  public Router use(int mask, Pattern path, String[] inUrlParams, CtxHandler[] mids) {

    List<HttpMethod> methods = HttpMethod.unMask(mask);

    for (HttpMethod m : methods) {

      List<Route> routes = this.routes.get(m.ordinal());

      List<CtxHandler> middles = S.array(mids);
      //build route
      Route route = new Route(path, inUrlParams, middles);

      //install the paramsGetters
      for( CtxHandler h: middles) {
        if( h instanceof WellDefinedHandler) {
          ((WellDefinedHandler) h).install(route);
        }
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
    defaultMids.addAll(Arrays.asList(mids));
    return this;
  }

  public void clean() {
    routes = new Routes();
  }


  @SuppressWarnings("unchecked")
  private static class Routes {

    final private List<Route>[] all = new List[HttpMethod.values().length];

    List<Route> get(int httpMethodOrdinal) {
      List ret = all[httpMethodOrdinal];
      if (ret == null) {
        ret = (all[httpMethodOrdinal] = new LinkedList<>());
      }
      return (List<Route>) ret;
    }
  }

}
