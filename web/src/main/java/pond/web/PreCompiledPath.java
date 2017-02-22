package pond.web;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

final public class PreCompiledPath{
  public final Pattern pattern;
  public final String[] names;

  PreCompiledPath(Pattern pattern, List<String> names) {
    this.pattern = pattern;
    this.names = names.toArray(new String[names.size()]);
  }

  PreCompiledPath(Pattern pattern, String... names) {
    this.pattern = pattern;
    this.names = names;
  }

  @Override
  public String toString() {
    return "PreCompiledPath{" +
        "pattern=" + pattern +
        ", names=" + Arrays.asList(names).toString() +
        '}';
  }
}

