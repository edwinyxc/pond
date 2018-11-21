package pond.web.api.contract;

import pond.web.router.HttpMethod;

import java.util.List;
import java.util.Set;

public class OperationObject {
    List<String> tags;
    int httpMask;
    String path;
    String summary;
    String description;
    String operationId;
    //requestBody
    Set<String> consumes;
    Set<String> produces;
    List<ParameterObject> parameters;
    List<ResponseObject> responses;
}
