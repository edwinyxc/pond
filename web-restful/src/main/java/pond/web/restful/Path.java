package pond.web.restful;

import pond.web.router.HttpMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by edwin on 3/12/2017.
 */
public class Path extends HashMap<String, Object> {

    final String path;

    public Path(String path) {
        this.path = path;
    }

    public Path method(HttpMethod method, Operation o) {
        this.put(method.name().toLowerCase(), o);
        return this;
    }

    public Operation method(HttpMethod method){
        return (Operation) this.get(method.name().toLowerCase());
    }

    /**
     * A list of parameters that are applicable for all the operations described under this path.
     * These parameters can be overridden at the operation level,
     * but cannot be removed there.
     * The list MUST NOT include duplicated parameters.
     * A unique parameter is defined by a combination of a name and location.
     * The list can handler the Reference Object to link to parameters that are defined at the Swagger Object's parameters.
     * There can be one "body" parameter at most.
     */
    public Path parameters(List<Parameter> parameters) {
        this.put("parameters", parameters);
        return this;
    }

    public List<Parameter> parameters() {
        List<Parameter> parameters = (List<Parameter>) this.get("parameters");
        if(parameters == null) {
            parameters = new ArrayList<>();
            this.parameters(parameters);
        }
        return parameters;
    }

}
