package pond.web.api.contract;
import pond.web.router.HttpMethod;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class PathContract {

    public HashMap<String, OperationObject> operations = new HashMap<>();

    public List<ParameterObject> parameters = new LinkedList<>();

}
