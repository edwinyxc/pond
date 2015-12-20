package pond.web.spi;

import pond.web.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Deprecated
/**
 * for pond version < 0.2.0
 */
public class FallbackPathToRegCompiler implements PathToRegCompiler{

  private static Pattern pattern_inPathVars = Pattern.compile("\\$\\{(\\w+)\\}");

  @Override
  public PreCompiledPath compile(String path) {

    List<String> names = new ArrayList<>();
    Matcher matcher = pattern_inPathVars.matcher(path);
    StringBuffer buffer = new StringBuffer();

    while (matcher.find()) {
      names.add(matcher.group(1));
      matcher.appendReplacement(buffer, "([^/]+)");
    }
    matcher.appendTail(buffer);

    return new PreCompiledPath(Pattern.compile(buffer.toString()),names);
  }

  @Override
  public String preparePath(Route entry_route, String path) {

    //procedure of nested routers
    //if entry_route is null, then this routing is a root routing
    if(entry_route != null) {
      String entry_path = entry_route.defPath().pattern();

      //search for the wildcards "/.*", any sub router should have it.
      if(!entry_path.endsWith("/.*")) {
        throw new RuntimeException("invalid router definition: the router must be prefixed with a regexp ending with /.*");
      }

      Pattern trimmed = Pattern.compile(entry_path.substring(0, entry_path.length() - 3));
      Matcher matcher = trimmed.matcher(path);
      if(matcher.find()){
        return path.substring(matcher.end());
      }
      else{
        //this would not happen
        throw new RuntimeException("This would not happen");
      }
    }

    return path;
  }


  public static void main(String[] args) {

  }

}
