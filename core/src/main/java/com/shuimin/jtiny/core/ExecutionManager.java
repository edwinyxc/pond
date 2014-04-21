package com.shuimin.jtiny.core;

public class ExecutionManager {
    final public static ThreadLocal<ExecutionContext> ExecutionContexts = new ThreadLocal<>();
}
