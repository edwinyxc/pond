package com.shuimin.pond.db;

import com.shuimin.common.S;

import java.util.Map;

/**
 * Created by ed on 7/15/14.
 */
public class TestRecord extends AbstractRecord{
    {
        table("t_crm_order");
        id("vid");
        field("create_time");
    }

    @Override
    public Map<String, Object> view() {
        Map<String,Object> ret = super.view();
        ret.put("human_date",
                S.date.fromLong((Long)get("create_time"),"yyyy-MM-dd hh:mm:ss"));
        return ret;
    }
}
