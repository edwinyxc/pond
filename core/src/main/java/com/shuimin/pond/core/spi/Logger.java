package com.shuimin.pond.core.spi;

import com.shuimin.common.SPILoader;

/**
 * Created by ed on 2014/5/7.
 */
public interface Logger {

    public static final String NAME = "pond";
    static boolean[] allows = {true, true, true, true};
    static final int DEBUG = 0;
    static final int FATAL = 1;
    static final int WARN = 2;
    static final int INFO = 3;

    public static Logger createLogger(Class clazz) {
        return SPILoader.service(Logger.class).get(clazz);
    }

    static boolean allowDebug() {
        return allows[DEBUG];
    }

    static void allowDebug(boolean b) {
        allows[DEBUG] = b;
    }

    static boolean allowInfo() {
        return allows[INFO];
    }

    static void allowInfo(boolean b) {
        allows[INFO] = b;
    }

    static boolean allowFatal() {
        return allows[FATAL];
    }

    static void allowFatal(boolean b) {
        allows[FATAL] = b;
    }

    static boolean allowWarn() {
        return allows[WARN];
    }

    static void allowWarn(boolean b) {
        allows[WARN] = b;
    }

    public Logger get(Class clazz);

    void debug(String o);

    void debug(Throwable e);

    void info(String o);

    void warn(String o);

    void fatal(String o);
}
