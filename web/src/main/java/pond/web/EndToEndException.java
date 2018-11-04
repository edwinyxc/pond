package pond.web;

/**
 * Created by ed on 1/16/16.
 */
public class EndToEndException extends RuntimeException{
    final public int http_status;
    final public String message;
    public Throwable cause;

    public EndToEndException(int code, String message, Throwable cause){
        this.http_status = code;
        this.message = message;
        this.cause = cause;
    }

    public EndToEndException(int code, String message){
        this.http_status = code;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
