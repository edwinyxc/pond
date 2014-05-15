package com.shuimin.pond.core.spi.logger;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by ed on 2014/5/7.
 */
public class JdkLogger
    implements com.shuimin.pond.core.spi.Logger {

    Logger logger = Logger.getLogger(NAME);

    @Override
    public void debug(String o) {
        if (allowDebug())
            logger.log(Level.FINE, o);
    }

    @Override
    public void debug(Throwable e) {
        if (allowDebug())
            logger.log(Level.FINE, e.getMessage(), e);
    }

    @Override
    public void warn(String o) {
        if (allowWarn())
            logger.log(Level.WARNING, o);
    }

    @Override
    public void fatal(String o) {
        if (allowFatal())
            logger.log(Level.SEVERE, o);
    }

    @Override
    public boolean allowDebug() {
        return logger.isLoggable(Level.FINE);
    }

    @Override
    public boolean allowInfo() {
        return logger.isLoggable(Level.INFO);
    }

    @Override
    public boolean allowFatal() {
        return logger.isLoggable(Level.SEVERE);
    }

    @Override
    public boolean allowWarn() {
        return logger.isLoggable(Level.WARNING);
    }

    @Override
    public void info(String o) {
        if (allowInfo())
            logger.log(Level.INFO, o);
    }
}
