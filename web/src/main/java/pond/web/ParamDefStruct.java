package pond.web;

import pond.common.S;
import pond.common.f.Function;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ed on 3/5/17.
 */

public class ParamDefStruct<A> extends ParamDef<A> {

    final Map<String, ParamDef> innerParamDefs;
    final Function<A, Map<String, Object>> funcParser;

    ParamDefStruct(String name, Function<A, Map<String, Object>> funcParser, List<ParamDef> inners) {
        super(name);
        this.funcParser = funcParser;
        this.innerParamDefs = new LinkedHashMap<>();
        for (ParamDef def : inners) {
            this.innerParamDefs.put(def.name, def);
        }
    }

    @Override
    public A get(Ctx c) {
        return funcParser.apply(S._for(this.innerParamDefs).map(paramDef -> paramDef.get(c)).val());
    }

}
