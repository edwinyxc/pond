package pond.web;

import pond.common.S;

import java.util.List;

/**
 * Created by ed on 3/5/17.
 */

class ParametrisedCtxHandler implements CtxHandler {

    final List<ParamDef> def ;
    final CtxHandler handler;

    ParametrisedCtxHandler(List<ParamDef> defs, CtxHandler handler ){
        this.def = defs;
        this.handler = handler;
    }

    void install(Route route) {
        S._for(def).each(route::addRequestParamNameAndTypes);
    }

    @Override
    public void apply(Ctx t) {
        handler.apply(t);
    }

}

