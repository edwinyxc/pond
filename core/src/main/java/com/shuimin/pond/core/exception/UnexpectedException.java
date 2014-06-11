package com.shuimin.pond.core.exception;

/**
 * @author ed
 */
public class UnexpectedException extends PondException {

    public UnexpectedException(String err, Throwable th) {
        super(err, th);
    }

    public UnexpectedException(String err) {
        super(err);
    }

    public UnexpectedException() {
        super();
    }

    public UnexpectedException(Throwable th) {
        super(th);
    }

    @Override
    public String brief() {
        return "an unexpected Exception has been thrown";
    }

}
