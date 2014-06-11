package com.shuimin.pond.core.exception;

@SuppressWarnings("serial")
public class HttpException extends PondException {

    private final int code;

    public HttpException() {
        this.code = 500;
    }

    public HttpException(Exception e) {
        this.code = 500;
    }

    public HttpException(int code, String errMsg) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    @Override
    public String brief() {
        return String.valueOf(code);
    }

    @Override
    public String detail() {
        return brief() + ": " + getMessage();
    }


}
