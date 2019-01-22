package pond.web.api.stereotype;

import java.util.Map;

public class RequestBodyObject {
    String description;
    Map<String, MediaTypeObject> content;
    String required;
}
