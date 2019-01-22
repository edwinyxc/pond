package pond.web.api.stereotype;

import java.util.List;

public class OpenAPIObject {
    /**
     * REQUIRED.
     * This string MUST be the semantic version number of the OpenAPI Specification version
     * that the OpenAPI document uses. The openapi field SHOULD be used by tooling
     * specifications and clients to interpret the OpenAPI document.
     * This is  not related to the API info.version string.
     */
    String openapi;

    InfoObject info;

    List<ServerObject> servers;

    PathsObject paths;

    ComponentsObject components;

    List<SecurityRequirementObject> security;

    List<TagObject> tags;

    ExternalDocumentObject externalDocs;

}
