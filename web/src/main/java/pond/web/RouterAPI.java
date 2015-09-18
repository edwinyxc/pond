package pond.web;

import pond.common.SPILoader;
import pond.web.http.HttpMethod;
import pond.web.spi.PathToRegCompiler;
import pond.web.spi.PreCompiledPath;

import java.util.regex.Pattern;

/**
 * Router  API
 * Holder of responsibility chain, also called business chain
 *
 * @param <E>
 */
public interface RouterAPI<E extends Router> {

  final static PathToRegCompiler compiler = SPILoader.service(PathToRegCompiler.class);

  /**
   * Add a middleware to Router
   *
   * @param mask Http Method Mask
   * @param path regular expr
   * @param mids middleware array
   * @return Router
   * @see pond.web.http.HttpMethod
   */
  E use(int mask, Pattern path, String[] inUrlParams, Mid[] mids);

  default E use(int mask, String path, Mid... mids) {
    PreCompiledPath preCompiledPath = compiler.compile(path);
    return use(mask, preCompiledPath.pattern, preCompiledPath.names, mids);
  }

  static Pattern all_through = Pattern.compile("/.*");
  static String[] empty_params = new String[0];

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

  /**
   * default call
   * THIS WILL BE CALLED IF NONE OF ABOVE MIDS FINISHED THE PROCESSING
   */
  E otherwise(Mid... mids);

}
