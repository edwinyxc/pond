package com.shuimin.table.spi;

/**
 * Created by ed on 6/4/14.
 * Get the name of the specified cell expression
 */
public interface ExpressionEngine {

    boolean isExpression(String possible);

    String getName(String expr);

}
