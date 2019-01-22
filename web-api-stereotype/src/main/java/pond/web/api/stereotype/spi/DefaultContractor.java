package pond.web.api.stereotype.spi;

import pond.web.api.stereotype.*;
import pond.web.router.Router;

import java.util.ArrayList;
import java.util.List;

import static pond.web.api.stereotype.ParameterInContract.*;
import static pond.web.api.stereotype.ParameterSchemaContract.*;

public class DefaultContractor implements Contractor {

    List<ParameterInContract<?>> param_in = new ArrayList<>();

    List<ParameterSchemaContract<?,?>> param_schema = new ArrayList<>();

    List<RouterContract> router = new ArrayList<>();

    List<PathContract> path = new ArrayList<>();

    List<OperationContract> operationContracts = new ArrayList<>();


    DefaultContractor(){
        this.param_in.addAll(List.of(
            BODY_MULTIPART_UPLOAD_FILE,
            BODY_MULTIPART_ATTRIBUTE,
            BODY,
            BODY_FORM,
            BODY_JSON,
            QUERY,
            PATH,
            HEADER,
            COOKIE
        ));

        this.param_schema.addAll(List.of(
            LONG_TO_DATE,
            STR_TO_DATE,
            INT,
            LONG,
            NUMBER,
            JSON,
            JSON_ARRAY,
            HTTP_ARRAY
        ));

    }

    @Override
    public List<ParameterInContract<?>> parameterInContracts() {
        return param_in;
    }

    @Override
    public List<ParameterSchemaContract<?, ?>> parameterSchemaContract() {
        return param_schema;
    }

    @Override
    public List<RouterContract> routerContracts() {
        return router;
    }

    @Override
    public List<PathContract> pathContracts() {
        return path;
    }

    @Override
    public List<OperationContract> operationContracts() {
        return operationContracts;
    }

    @Override
    public Router build(Object contract) {
        return ;
    }

}
