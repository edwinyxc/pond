package pond.db;

import pond.common.S;

import java.util.Map;

/**
 * Created by ed on 9/29/14.
 */
public class TestModel extends Model {
    {
        table("t_pay");
        id("id");
        field("create_time");
    }

    @Override
    public Map<String, Object> view() {
        Map<String, Object> ret = super.view();
        ret.put("human_date",
                S.date.fromLong((Long) get("create_time"), "yyyy-MM-dd hh:mm:ss"));
        return ret;
    }
}
