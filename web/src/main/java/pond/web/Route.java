//package pond.web;
//
//import pond.common.f.Callback;
//import pond.common.f.Function;
//import pond.web.http.HttpMethod;
//
//import java.util.*;
//import java.util.regex.Pattern;
//
///**
// * Route node
// */
//public class Route {
//
//    public final HttpMethod method;
//    private final List<CtxHandler> mids;
//    private final Pattern definition;
//    private final String basePath;
//    private final String[] inUrlParamNames;
////    private final Map<String, Function<?, Context>> paramDefs;
////    private final List<Callback.C2<Context, ?>> resultDefs;
//
//    /**
//     * Returns the definition path
//     */
//    public Pattern defPath() {
//        return definition;
//    }
//
//    public String basePath() {
//        return basePath;
//    }
//
//    public Route(HttpMethod method, Pattern def, String[] inUrlNames, String basePath, List<CtxHandler> mids) {
//        this.method = method;
//        definition = def;
//        this.basePath = basePath;
//        inUrlParamNames = inUrlNames;
////        paramDefs = new LinkedHashMap<>();
////        resultDefs = new LinkedList<>();
//        this.mids = mids;
//    }
//
////    void addRequestParamNameAndTypes(String name, Function<?, Context> paramDef) {
////        this.paramDefs.put(name, paramDef);
////    }
////
////    void addResultDef(Callback.C2<Context, ?> resultDef) {
////        this.resultDefs.add(resultDef);
////    }
////
////    public List<Callback.C2<Context, ?>> resultDefs() {
////        return resultDefs;
////    }
////
////    public Map<String, Function<?, Context>> paramDefs() {
////        return paramDefs;
////    }
//
//    public List<CtxHandler> handlers() {
//        return mids;
//    }
//
//    RegPathMatchResult match(String path) {
//        return RegPathMatcher.match(definition, path, inUrlParamNames);
//    }
//
//    @Override
//    public String toString() {
//        return "Route{" +
//                "handlers=" + mids +
//                ", definition=" + definition +
//                ", inUrlParamNames=" + Arrays.toString(inUrlParamNames) +
//                '}';
//    }
//}
