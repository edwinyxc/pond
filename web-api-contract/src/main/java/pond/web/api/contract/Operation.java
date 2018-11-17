package pond.web.api.contract;

import java.util.List;
import java.util.Set;

public class Operation {
    List<String> tags;
    String summary;
    String description;
    String id;
    Set<String> consumes;
    Set<String> produces;
    List<Parameter<?>> parameters;
    List<Response<?>> responses;
}
