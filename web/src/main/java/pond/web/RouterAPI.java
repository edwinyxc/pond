package pond.web;

import pond.web.http.HttpMethod;

/**
 * Router  API
 * Holder of responsibility chain, also called business chain
 *
 * @param <E>
 */
public interface RouterAPI<E extends Router> {

  /**
   * Add a middleware to Router
   *
   * @param mask Http Method Mask
   * @param path regular expr
   * @param mids middleware array
   * @return Router
   * @see pond.web.http.HttpMethod
   */
  E use(int mask, String path, Mid... mids);

  /**
   * Add a sub router at responsibility chain
   *
   * @param path regular expr
   * @return Router
   */
  E use(String path, Router router);

  default E use(String path, Mid... mids) {
    return use(HttpMethod.maskAll(), path, mids);
  }

  default E use(Mid... mids) {
    return use(HttpMethod.maskAll(), "/.*", mids);
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

}
