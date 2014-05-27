package com.shuimin.pond.codec.sql;

import com.shuimin.common.f.Tuple;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ed on 14-5-22.
 */
public abstract class AbstractSql implements Sql{

    @Override
    public String toString() {
        return preparedSql();
    }

}
