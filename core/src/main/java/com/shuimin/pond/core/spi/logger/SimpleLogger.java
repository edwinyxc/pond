package com.shuimin.pond.core.spi.logger;

import com.shuimin.common.S;
import com.shuimin.pond.core.spi.Logger;

/**
 * Created by ed on 2014/5/7.
 */
public class SimpleLogger implements Logger {

    final com.shuimin.common.util.logger.Logger logger
        = S.logger().config("default",
        com.shuimin.common.util.logger.Logger.DEBUG);


    @Override
    public void debug(String o) {
        if(allowDebug())
        logger.debug(o);
    }

    @Override
    public void debug(Throwable e) {
        if(allowDebug())
        logger.debug(e.toString());
    }

    @Override
    public void info(String o) {
        if(allowInfo())
        logger.info(o);
    }

    @Override
    public void warn(String o) {
        if(allowWarn())
        logger.err(o);
    }

    @Override
    public void fatal(String o) {
        if(allowFatal())
        logger.fatal(o);
    }

}
