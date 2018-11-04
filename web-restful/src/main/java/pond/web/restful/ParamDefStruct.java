package pond.web.restful;

import pond.common.S;
import pond.common.f.Function;
import pond.web.http.HttpCtx;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ed on 3/5/17.
 */

public class ParamDefStruct<A> extends ParamDef<A> {

    private final Map<String, ParamDef> schema;
    public final Function<A, Map<String, Object>> funcParser;

    ParamDefStruct(String name, Function<A, Map<String, Object>> funcParser, List<ParamDef> inners) {
        super(name);
        this.funcParser = funcParser;
        this.schema = new LinkedHashMap<>();
        for (ParamDef def : inners) {
            this.schema.put(def.name, def);
        }
        this.handler = this::get;
    }

    public List<ParamDef> defs() {
        return S._for(schema.entrySet()).map(Map.Entry::getValue).toList();
    }

    public Schema schema(){
        return Schema.PARAMS_SCHEMA(schema);
    }

    @Override
    public A get(HttpCtx c) {
        return funcParser.apply(S._for(this.schema).map(paramDef -> paramDef.get(c)).val());
    }

}
