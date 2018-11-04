package pond.web.router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.S;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse a regular-expressed path to parameters map
 */
public class RegPathMatcher {

  final static Logger logger = LoggerFactory.getLogger(RegPathMatcher.class);

//  static PathToRegCompiler compiler = SPILoader.service(PathToRegCompiler.class);

//  static Cache<String, PreCompiledPath> cache = Cache.lruCache(1000);

  /**
   * Match a path with it's corresponding reg-exp definition.
   *
   * @param pattern -- a representing string handle req-exp
   * @param path    -- string handle path waiting for check
   * @return -- Match ResultDef
   */
  public static RegPathMatchResult match(Pattern pattern, String path, String[] params) {

    S._assertNotNull(pattern, path);

//    PreCompiledPath preCompiledPath = cache.get(pattern);
//    if (preCompiledPath == null) {
//      preCompiledPath = compiler.compile(pattern);
//      cache.put(pattern, preCompiledPath);
//    }

    Matcher matcher = pattern.matcher(path);
    int groupCount = matcher.groupCount();

    params = (params == null ? S._tap(new String[groupCount], arr -> {
      for (int i = 0; i < arr.length; i++) {
        arr[i] = String.valueOf(i);
      }
    }) : params);

    if (matcher.matches()) {
      //this is a 'through'
      String name;
      String val;
      Map<String, String> ret = new HashMap<>();
      for (int i = 0; i < groupCount && i < params.length; i++) {
        name = params[i];
        //group(0) must be the whole input, captured group starts from 1
        val = matcher.group(i + 1);
        ret.put(name, val);
      }
      return new RegPathMatchResult(true, ret);
    }

    return RegPathMatchResult.NEGATIVE;

  }


}
