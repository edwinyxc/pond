package pond.core.exception;

@SuppressWarnings("serial")
public class HttpException extends PondException {

    private final int code;

    public HttpException() {
        super();
        this.code = 500;
    }

    public HttpException(Exception e) {
        super(e);
        this.code = 500;
    }

    public HttpException(int code, String errMsg) {
        super(errMsg);
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
