package pond.web.api.stereotype;

import pond.web.router.Router;

import java.util.List;

public interface Contractor {

    List<ParameterInContract<?>> parameterInContracts();

    List<ParameterSchemaContract<?,?>> parameterSchemaContract();

    List<RouterContract> routerContracts();

    List<PathContract> pathContracts();

    List<OperationContract> operationContracts();

    Router build(Object contract);
}
