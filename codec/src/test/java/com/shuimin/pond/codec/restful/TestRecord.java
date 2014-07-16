package com.shuimin.pond.codec.restful;

import com.shuimin.pond.db.AbstractRecord;

/**
 * Created by ed on 7/16/14.
 */
public class TestRecord extends AbstractRecord {
    {
        table("test");
        id("id");
        field("name");
    }
}
