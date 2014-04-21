package com.shuimin.jtiny.core.exception;

/**
 * @author ed
 */
public class UnexpectedException extends YException {

    public UnexpectedException(Object cause) {
        super(cause);
    }

    @Override
    public String brief() {
        return "an unexpected Exception has been thrown";
    }

}
