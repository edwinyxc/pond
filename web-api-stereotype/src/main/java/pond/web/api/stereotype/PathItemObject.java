package pond.web.api.stereotype;

import java.util.List;

public class PathItemObject {

    String $ref;
    String summary;

    String description;

    OperationObject get;
    OperationObject put;
    OperationObject post;
    OperationObject delete;
    OperationObject options;
    OperationObject head;
    OperationObject patch;
    OperationObject trace;

    /**
     * An alternative server array to service all operations in this path
     */
    List<ServerObject> servers;

    /**
     * A list of parameters that are applicable for all the operations
     * described under this path.
     * These parameters can be overridden at the operation level,
     * but cannot be removed there.
     * The list MUST NOT include duplicated parameters.
     * A unique parameter is defined by a combination of a name and location.
     * The list can use the Reference Object to link to parameters that are defined at
     * the OpenAPI Object's components/parameters.
     */
    List<ParameterObject> parameters;

}
