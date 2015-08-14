package com.shuimin.table.spi.expr;

import com.shuimin.table.spi.ExpressionEngine;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ed on 6/4/14.
 */
public class SimpleExprEngine implements ExpressionEngine {

  private static Pattern varPattern = Pattern.compile("\\$\\{(.*)\\}");

  @Override
  public boolean isExpression(String possible) {
    return varPattern.matcher(possible).matches();
  }

  @Override
  public String getName(String expr) {
    Matcher matcher = varPattern.matcher(expr);
    if (matcher.find()) {
      String ret = matcher.group(1);
      return matcher.group(1);
    }
    return "";
  }

  public static void main(String[] arg) {
    String test = "${name" +
        "}";
    SimpleExprEngine engine = new SimpleExprEngine();
    System.out.println(engine.isExpression(test));
    System.out.println(engine.getName(test));
  }
}
