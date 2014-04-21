package com.shuimin.jtiny.codec.db;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by ed on 2014/4/18.
 */
public interface RowMapper {
    public Record map(ResultSet rs) throws SQLException;
}
