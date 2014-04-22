package com.shuimin.pond.core.exception;

@SuppressWarnings("serial")
public class HttpException extends YException {

    private final int code;

    public int code() {
        return code;
    }

    public HttpException() {
        super("error");
        this.code = 500;
    }

    public HttpException(Exception e) {
        super(e.getMessage());
        this.code = 500;
    }

    public HttpException(int code, String errMsg) {
        super(errMsg);
        this.code = code;
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
