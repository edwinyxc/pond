package pond.db;


/**
 * Created by ed on 6/23/14.
 */
public class RuntimeSQLException extends RuntimeException{

    public RuntimeSQLException() {
    }

    public RuntimeSQLException(String message) {
        super(message);
    }

    public RuntimeSQLException(String message, Throwable cause) {
        super(message, cause);
    }

    public RuntimeSQLException(Throwable cause) {
        super(cause);
    }

}
