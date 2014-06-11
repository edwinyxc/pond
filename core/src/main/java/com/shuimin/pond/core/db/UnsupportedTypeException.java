package com.shuimin.pond.core.db;

import java.lang.reflect.Type;
import java.sql.SQLException;

import static com.shuimin.common.S.dump;

/**
 * Created by ed on 2014/4/24.
 */
public class UnsupportedTypeException extends SQLException {

    final Type t;
    final Type[] compatibleTypes;

    public UnsupportedTypeException(Type t, Type[] compatibleTypes) {
        this.t = t;
        this.compatibleTypes = compatibleTypes;
    }

    @Override
    public String getMessage() {
        return "found invalid type " + t + " allowed types are " + dump(compatibleTypes);
    }
}
