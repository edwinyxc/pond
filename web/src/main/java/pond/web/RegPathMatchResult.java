package pond.web;

import java.util.Map;

public class RegPathMatchResult {
  /*
  *TODO: could be more elegant by add type information into the PreCompiledPath
  //final Map<String,Object> params;
  */
  final Map<String, String> params;
  final boolean matches;

  final static RegPathMatchResult NEGATIVE = new RegPathMatchResult(false,null);

  public RegPathMatchResult(boolean matches, Map<String, String> params) {
    this.params = params;
    this.matches = matches;
  }

  @Override
  public String toString() {
    return "RegPathMatchResult{" +
        "params=" + params +
        ", matches=" + matches +
        '}';
  }
}

