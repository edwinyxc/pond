package pond.web.spi;

import pond.common.S;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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


  public static void main(String[] args) {

  }

}
