package pond.web;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Route node
 */
public class Route {

  private final List<CtxHandler> mids;
  private final Pattern definition;
  private final String[] inUrlParamNames;

  /**
   * Returns the definition path
   */
  public Pattern defPath() {
    return definition;
  }

  public Route(Pattern def, String[] names, List<CtxHandler> mids) {
    definition = def;
    inUrlParamNames = names;
    this.mids = mids;
  }

  List<CtxHandler> mids() {
    return mids;
  }

  RegPathMatchResult match(String path) {
    return RegPathMatcher.match(definition, path, inUrlParamNames);
  }

  @Override
  public String toString() {
    return "Route{" +
        "mids=" + mids +
        ", definition=" + definition +
        ", inUrlParamNames=" + Arrays.toString(inUrlParamNames) +
        '}';
  }
}
