package pond.web.api.stereotype;

import java.util.Map;

public class LinkObject {
    String operationRef;
    String operationId;
    Map<String, Object> parameters;
    Object requestBody;
    String description;
    ServerObject server;
}
