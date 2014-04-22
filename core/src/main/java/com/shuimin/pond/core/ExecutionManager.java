package com.shuimin.pond.core;

public class ExecutionManager {
    final public static ThreadLocal<ExecutionContext> ExecutionContexts = new ThreadLocal<>();
}
