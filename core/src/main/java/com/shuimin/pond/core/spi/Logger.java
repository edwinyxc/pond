package com.shuimin.pond.core.spi;

/**
 * Created by ed on 2014/5/7.
 */
public interface Logger {

    public static final String NAME = "pond";

    boolean[] allows = {false, true, true, true};

    static final int DEBUG = 0;
    static final int FATAL = 1;
    static final int WARN = 2;
    static final int INFO = 3;


    void debug(String o);

    void debug(Throwable e);

    void info(String o);

    void warn(String o);

    void fatal(String o);

    default boolean allowDebug() {
        return allows[DEBUG];
    }

    default void allowDebug(boolean b) {
        allows[DEBUG] = b;
    }

    default boolean allowInfo(){
        return allows[INFO];
    }

    default void allowInfo(boolean b){
        allows[INFO] = b;
    }

    default boolean allowFatal() {
        return allows[FATAL];
    }

    default void allowFatal(boolean b) {
        allows[FATAL] = b;
    }

    default boolean allowWarn() {
        return allows[WARN];
    }

    default void allowWarn(boolean b) {
        allows[WARN] = b;
    }
}
