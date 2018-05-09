package pond.web.restful;

import java.util.HashMap;
import java.util.List;

/**
 * Created by edwin on 3/12/2017.
 */
class SecurityRequirement extends HashMap<String, Object> {
    public SecurityRequirement(String name, List<String> content) {
        this.put("name", content);
    }
}
