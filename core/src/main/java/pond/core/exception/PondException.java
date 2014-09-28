package pond.core.exception;



/**
 * @author ed
 */
public abstract class PondException extends RuntimeException {

    public PondException() {
        super();
    }

    public PondException(String err) {
        super(err);
    }

    public PondException(Throwable th) {
        super(th);
    }

    public PondException(String err, Throwable th) {
        super(err, th);
    }

    public String brief() {
        return " an " + this.getClass().getSimpleName() + " Exception occured";
    }

    public String detail() {
        return ": caused by " + this.getCause().toString();
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
