package pond.web.api.stereotype;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class OperationObject {
    List<String> tags;
    int httpMask;
    String path;
    String summary;
    String description;
    ExternalDocumentObject externalDocs;
    String operationId;
    List<ParameterObject> parameters;
    RequestBodyObject requestBody;
    ResponsesObject responses;
    Map<String, CallbackObject> callbacks;

    boolean deprecated;
    SecurityRequirementObject security;

    Set<String> consumes;
    Set<String> produces;
    //List<ResponseObject> responses;
}
