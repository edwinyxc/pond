package pond.web;

import org.junit.Test;
import pond.common.S;
import pond.common.f.Array;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegToRouterTest {

  @Test
  public void test_raw_exp() {
    Pattern pattern = Pattern.compile("/(\\w+)/(\\w+)");
    Matcher matcher =  pattern.matcher("/renren.com/sssid");
    S.echo(matcher.matches());
    S.echo(matcher.groupCount());
    if (matcher.matches()) {
      for (int i = 0; i < matcher.groupCount(); i++) {
        S.echo(matcher.group(i + 1));
      }
    }
  }
//  @Test
//  public void test_fallback() {
//
//    PathToRegCompiler compiler = new FallbackPathToRegCompiler();
//    PreCompiledPath compiledPath = compiler.compile("/${_id}/${sss}");
//    S.echo(S.dump(compiledPath));
//    Matcher matcher = compiledPath.pattern.matcher("/renren.com/sss_id");
//    S.echo(matcher.matches());
//    S.echo(matcher.groupCount());
//    if (matcher.find()) {
//      for (int i = 0; i < matcher.groupCount(); i++) {
//        S.echo(matcher.group(i + 1));
//      }
//    }
//  }

  private List<String> find_groups (Pattern pattern, String input){
    List<String> re = new Array<>();
    Matcher matcher = pattern.matcher(input);
    if(matcher.find()) {
      for(int i = 0; i < matcher.groupCount(); i++)
        re.add(matcher.group(i+1));
    }
    return re;
  }

  @Test
  public void test_parse_reg() {
    S.echo(find_groups(Pattern.compile("/sddd"),"/sddd/ddd/ssss"));
  }

  private RegPathMatchResult match(PreCompiledPath pre, String path){
    S.echo(pre);
    return RegPathMatcher.match(pre.pattern,path,pre.names);
  }

  @Test
  public void test_express_style_parse() {
//    String expr = "([/.])?(?:(?::(\\w+)(?:\\(((?:\\\\.|[^()])+)\\))?|\\(((?:\\\\.|[^()])+)\\))([+*?])?|(\\*))";
//    Pattern p = Pattern.compile(expr);
//    S.echo(find_groups(p,"/:test"));
//    S.echo(find_groups(p,"/test"));
//    S.echo(find_groups(p,"/:test(\\\\d+)?"));
//    S.echo(find_groups(p,"/www/:name/:yxc"));
//    S.echo(find_groups(p,"/*"));

    ExpressPathToRegCompiler compiler = new ExpressPathToRegCompiler();
    compiler.strict(true);
    S.echo(match(compiler.compile("/users/a:name"),"/users/askygufei"));
    S.echo(match(compiler.compile("/users/:name#:password"),"/users/skygufei#gf"));
    S.echo(match(compiler.compile("/users/:name/:password"),"/users/name"));

    S.echo(match(compiler.compile("/users/:name/:password/"),"/users/name/pass"));

    S.echo(match(compiler.compile("/users/:name/:password"),"/users/name/pass/"));

    S.echo(match(compiler.compile("/users/:name/:password/"),"/users/name/pass/"));
    S.echo(match(compiler.compile("/users/:name/:password"),"/users/name/pass"));
  }




}
