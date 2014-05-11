package com.shuimin.pond.core.spi.logger;

import com.shuimin.common.S;
import com.shuimin.pond.core.spi.Logger;

/**
 * Created by ed on 2014/5/7.
 */
public class SimpleLogger implements Logger {

    final com.shuimin.common.util.logger.Logger logger
        = S.logger();

    @Override
    public void debug(String o) {

    }

    @Override
    public void debug(Throwable e) {

    }

    @Override
    public void info(String o) {

    }

    @Override
    public void warn(String o) {

    }

    @Override
    public void fatal(String o) {

    }

    @Override
    public boolean allowDebug() {
        return false;
    }

    @Override
    public boolean allowInfo() {
        return false;
    }

    @Override
    public boolean allowfatal() {
        return false;
    }

    @Override
    public boolean allowWarn() {
        return false;
    }
}
