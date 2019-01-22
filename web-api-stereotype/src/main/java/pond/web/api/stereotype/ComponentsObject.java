package pond.web.api.stereotype;

import java.util.Map;

public class ComponentsObject {
    //fixed fields
    Map<String, SchemaObject> schemas;
    Map<String, ResponseObject> responses;
    Map<String, ParameterObject> parameters;
    Map<String, ExampleObject> examples;
    Map<String, RequestBodyObject> requestBodies;
    Map<String, HeaderObject> headers;
    Map<String, SecuritySchemeObject> securitySchemes;
    Map<String, LinkObject> links;
    Map<String, CallbackObject> callbacks;
}
