package pond.web.spi;

import pond.common.S;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * draw from npm- module ‘path-to-regexp'
 * https://www.npmjs.com/package/path-to-regexp
 * LICENSE MIT
 * ORIGINAL AUTHOR : blakeembrey hello@blakeembrey.com
 */
public class ExpressPathToRegCompiler implements PathToRegCompiler{

  final static Pattern pattern =
      Pattern.compile("(\\\\.)|(/)?(?:(?::(\\w+)(?:\\(((?:\\\\.|[^()])+)\\))?|\\(((?:\\\\.|[^()])+)\\))([+*?])?|(\\*))");

  private boolean strict;
  private boolean end;

  public ExpressPathToRegCompiler strict(boolean strict){
    this.strict = strict;
    return this;
  }

  public ExpressPathToRegCompiler end(boolean end){
    this.end = end;
    return this;
  }

  @Override
  public PreCompiledPath compile(String path) {
    Matcher matcher = pattern.matcher(path);
    StringBuffer buffer = new StringBuffer();
    List<String> keys = S.array();
    while (matcher.find()){
      String escaped = matcher.group(1);

      if(escaped != null && escaped.length() > 0){
        continue;
      }

      String m = matcher.group();
      String prefix = matcher.group(2);
      String name = matcher.group(3);
      String capture = matcher.group(4);
      String group = matcher.group(5);
      String suffix = matcher.group(6);
      String asterisk = matcher.group(7);

      boolean repeat = "+".equals(suffix) || "*".equals(suffix);
      boolean optional = "?".equals(suffix) || "*".equals(suffix);

      String delimiter = (prefix == null ? "/" : prefix);
      String pattern = capture != null ? capture:( group != null ? group : (asterisk != null ? ".*" : "[^" + delimiter +  "]+?") );

      if(name == null){
        throw new NullPointerException("name");
      }

      keys.add(name);

      //avoid null
      prefix = S.avoidNull(prefix, "");

      StringBuilder replacement = new StringBuilder();
      if(repeat){
        pattern +="(?:" + prefix + pattern + ")*";
      }

      if(optional){
        if(prefix != null) {
          replacement.setLength(0);
          replacement.append("(?:").append(prefix)
              .append("(").append(pattern).append("))?");
        }
        else {
          replacement.setLength(0);
          replacement.append("(").append(pattern).append(")?");
        }
      }else {
        replacement.setLength(0);
        replacement.append(prefix).append("(").append(pattern).append(")");
      }
      matcher.appendReplacement(buffer, replacement.toString());

    }
    matcher.appendTail(buffer);

    String route = buffer.toString();
    boolean endsWithSlash = route.endsWith("/");
    // In non-strict mode we allow a slash at the end of match. If the path to
    // match already ends with a slash, we remove it for consistency. The slash
    // is valid at the end of a path match, not in the middle. This is important
    // in non-ending mode, where "/test/" shouldn't match "/test//route".
    if (!strict) {
      route = (endsWithSlash ? route.substring(0, route.length() - 1) : route)
          + "(?:/(?=$))?";
    }

    if (end) {
      route += '$';
    } else {
      // In non-ending mode, we need the capturing groups to match as much as
      // possible by using a positive lookahead to the end or next path segment.
      route += strict && endsWithSlash ? "" : "(?=/|$)";
    }

    return new PreCompiledPath(Pattern.compile("^"+route), keys);

  }


}
