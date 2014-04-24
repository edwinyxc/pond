package com.shuimin.pond.codec.db;

import java.lang.reflect.Type;
import java.sql.SQLException;

import static com.shuimin.common.S.dump;

/**
 * Created by ed on 2014/4/24.
 */
public class UnsupportedTypeException extends SQLException {
    final Type t ;

    public UnsupportedTypeException(Type t, Type[] compactibleTypes) {
        this.t = t;
        this.compactibleTypes = compactibleTypes;
    }

    final Type[] compactibleTypes ;

    @Override
    public String getMessage() {
        return "found invalid type "+ t + " allowed types are "+ dump(compactibleTypes);
    }
}
