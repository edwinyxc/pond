package pond.web.api.contract;

import pond.web.router.HttpMethod;

import java.util.List;
import java.util.Map;

public class PathObject {

    public String summary;
    public String description;

    public List<ParameterObject> parameters;

    public Map<HttpMethod, OperationObject>  operations;

}
