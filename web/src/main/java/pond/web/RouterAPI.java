package pond.web;

import pond.common.S;
import pond.common.SPILoader;
import pond.web.http.HttpMethod;

import java.util.regex.Pattern;

/**
 * Router  API
 * Holder of responsibility chain, also called business chain
 *
 * @param <E>
 */
public interface RouterAPI<E extends Router> {

  final static PathToRegCompiler compiler = new ExpressPathToRegCompiler();

  static Pattern all_through = Pattern.compile("/.*");
  static String[] empty_params = new String[0];

  /**
   * Add a middleware to Router
   *
   * @param mask     Http Method Mask
   * @param path     regular expr
   * @param handlers ctx-handlers array
   * @return Router
   * @see pond.web.http.HttpMethod
   */
  E use(int mask, Pattern path, String[] inUrlParams, CtxHandler[] handlers);

  default E use(int mask, Pattern path, String[] inUrlParams, Mid... mids) {
    return use(mask, path, inUrlParams, S._for(mids).map(CtxHandler::mid).join());
  }

  default E use(int mask, String path, Mid... mids) {
    PreCompiledPath preCompiledPath = compiler.compile(path);
    return use(mask, preCompiledPath.pattern, preCompiledPath.names, mids);
  }

  default E use(int mask, String path, CtxHandler... mids) {
    PreCompiledPath preCompiledPath = compiler.compile(path);
    return use(mask, preCompiledPath.pattern, preCompiledPath.names, mids);
  }

  default E use(CtxHandler... mids) {
    return use(HttpMethod.maskAll(), all_through, empty_params, mids);
  }

  default E use(Mid... mids) {
    return use(HttpMethod.maskAll(), all_through, empty_params, mids);
  }

  default E use(String path, Mid... mids) {
    return use(HttpMethod.maskAll(), path, mids);
  }

  default E get(String path, Mid... mids) {
    return use(HttpMethod.mask(HttpMethod.GET), path, mids);
  }

  default E post(String path, Mid... mids) {
    return use(HttpMethod.mask(HttpMethod.POST), path, mids);
  }

  default E del(String path, Mid... mids) {
    return use(HttpMethod.mask(HttpMethod.DELETE), path, mids);
  }

  default E put(String path, Mid... mids) {
    return use(HttpMethod.mask(HttpMethod.PUT), path, mids);
  }

  default E use(String path, CtxHandler... mids) {
    return use(HttpMethod.maskAll(), path, mids);
  }

  default E get(String path, CtxHandler... mids) {
    return use(HttpMethod.mask(HttpMethod.GET), path, mids);
  }

  default E post(String path, CtxHandler... mids) {
    return use(HttpMethod.mask(HttpMethod.POST), path, mids);
  }

  default E del(String path, CtxHandler... mids) {
    return use(HttpMethod.mask(HttpMethod.DELETE), path, mids);
  }

  default E put(String path, CtxHandler... mids) {
    return use(HttpMethod.mask(HttpMethod.PUT), path, mids);
  }
  /**
   * default call
   * THIS WILL BE CALLED IF NONE OF ABOVE MIDS FINISHED THE PROCESSING
   */
  E otherwise(CtxHandler... mids);

  default E otherwise(Mid... mids) {
    return otherwise(S._for(mids).map(CtxHandler::mid).join());
  }
}
