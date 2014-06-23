package com.shuimin.pond.core.db;

import com.shuimin.pond.core.exception.PondException;

/**
 * Created by ed on 6/23/14.
 */
public class RuntimeSQLException extends PondException{

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
