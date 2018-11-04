package pond.web.router;

import java.util.Map;

public class RegPathMatchResult {
  /*
  *TODO: could be more elegant by add type information into the PreCompiledPath
  //final Map<String,Object> queries;
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
        "queries=" + params +
        ", matches=" + matches +
        '}';
  }
}

