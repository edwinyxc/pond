package pond.core.exception;

/**
 * Created by ed on 8/27/14.
 * User input error
 */
public class UserException extends HttpException{
    public UserException(String errMsg) {
        //bad request
        super(400,errMsg);
    }
}
