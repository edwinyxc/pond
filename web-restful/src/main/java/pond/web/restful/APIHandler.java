package pond.web.restful;

import pond.web.Ctx;
import pond.web.CtxHandler;

import java.util.Collections;
import java.util.List;

/**
 * Created by ed on 3/5/17.
 */

public class APIHandler implements CtxHandler {

    final List<ParamDef> paramDefs;
    final List<ResultDef> resultDefs;
    final CtxHandler handler;

    public APIHandler(List<ParamDef> defs, CtxHandler handler) {
        this.paramDefs = defs;
        this.resultDefs = Collections.emptyList();
        this.handler = handler;
    }

    public <R> APIHandler(List<ParamDef> defs, List<ResultDef> results, CtxHandler handler) {
        this.paramDefs = defs;
        this.resultDefs = results;
        this.handler = handler;
    }

    @Override
    public void apply(Ctx t) {
        handler.apply(t);
    }

}

