package pond.codec.restful;

import pond.db.Model;

/**
 * Created by ed on 7/16/14.
 */
public class TestRecord extends Model {
    {
        table("test");
        id("id");
        field("name");
    }
}
