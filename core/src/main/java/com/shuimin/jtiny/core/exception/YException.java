package com.shuimin.jtiny.core.exception;

import com.shuimin.jtiny.core.Server.G;

/**
 * @author ed
 */
public abstract class YException extends RuntimeException {

    final Object cause;

    public YException(Object cause) {
        this.cause = cause;
    }

    public String brief() {
        return this.getMessage();
    }

    public String detail() {
        return ": caused by " + cause().toString();
    }

    //show where the Exception has been thrown
    public final Object cause() {
        return cause;
    }

    @Override
    public String toString() {
        return G.debug() ? detail() : brief();
    }
}
