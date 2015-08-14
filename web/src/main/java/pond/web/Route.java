package pond.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 路由节点
 */
public class Route {
  private static Pattern varPattern = Pattern.compile("\\$\\{(\\w+)\\}");
  Pattern def;
  List<String> pathVarNames = new ArrayList<>();
  List<Mid> mids;
  String def_path;

  public Route(String path, List<Mid> mids) {
    def_path = Pond._ignoreLastSlash(path);
    compile();
    this.mids = mids;
  }

  private void compile() {
    pathVarNames.clear();
    Matcher matcher = varPattern.matcher(def_path);
    StringBuffer buffer = new StringBuffer();
    while (matcher.find()) {
      pathVarNames.add(matcher.group(1));
      matcher.appendReplacement(buffer, "([^/]+)");
    }
    matcher.appendTail(buffer);
    this.def = Pattern.compile(buffer.toString());
  }

  Route prefix(String prefix) {
    def_path = Pond._ignoreLastSlash(prefix + def_path);
    compile();
    return this;
  }


  /**
   * 返回用户对于该节点的定义,正则表达式
   */
  public Pattern def() {
    return def;
  }

  /**
   * url中的参数名
   */
  List<String> urlParamNames() {
    return pathVarNames;
  }

  /**
   * 获取定义在url中的参数
   */
  Map<String, String> urlParams(String path) {
    Map<String, String> ret = new TreeMap<>();
    Matcher matcher =
        def().matcher(path);
    List<String> url_p_names = urlParamNames();
    if (matcher.matches()) {
      for (int i = 0; i < matcher.groupCount(); i++) {
        ret.put(url_p_names.get(i), matcher.group(i + 1));
      }
    }
    return ret;
  }

  public boolean match(String path) {
    return def.matcher(path).matches();
  }

  @Override
  public String toString() {
    return "Route@" + hashCode() + "{" +
        "def=" + def + "," +
        "mid=" + mids.toString() +
        '}';
  }
}
