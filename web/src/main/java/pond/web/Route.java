package pond.web;

import pond.common.S;
import pond.common.f.Function;
import pond.common.f.Tuple;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Route node
 */
public class Route {

    private final List<CtxHandler> mids;
    private final Pattern definition;
    private final String[] inUrlParamNames;
    private final Map<String, Tuple<Class, Function<?, Ctx>>> inRequestParamGetters;

    /**
     * Returns the definition path
     */
    public Pattern defPath() {
        return definition;
    }

    public Route(Pattern def, String[] names, List<CtxHandler> mids) {
        definition = def;
        inUrlParamNames = names;
        inRequestParamGetters = new LinkedHashMap<>();
        this.mids = mids;
    }

    void addRequestParamNameAndTypes(ParamDef paramDef) {
        this.inRequestParamGetters.put(paramDef.name(), Tuple.pair(paramDef.type(), paramDef::get));
    }

    public Map<String, Tuple<Class, Function<?, Ctx>>> inRequestParamGetters() {
        return inRequestParamGetters;
    }

    List<CtxHandler> mids() {
        return mids;
    }

    RegPathMatchResult match(String path) {
        return RegPathMatcher.match(definition, path, inUrlParamNames);
    }

    @Override
    public String toString() {
        return "Route{" +
                "mids=" + mids +
                ", definition=" + definition +
                ", inUrlParamNames=" + Arrays.toString(inUrlParamNames) +
                ", inRequestParamGetters=" + S.dump(inRequestParamGetters) +
                '}';
    }
}
