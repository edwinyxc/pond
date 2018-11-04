package pond.web.router;

import pond.common.S;
import pond.core.CtxHandler;
import pond.web.Mid;

import java.util.regex.Pattern;

/**
 * Router  API
 * Holder handle responsibility chain, also called business chain
 *
 * @param <ROUTER>
 */
public interface RouterAPI<ROUTER extends Router> {

    PathToRegCompiler compiler = new ExpressPathToRegCompiler();

    Pattern all_through = compiler.compile("/*").pattern;
    String[] empty_params = new String[0];

    void configRoute(Route route, CtxHandler use);

    /**
     * Add a middleware to Router
     *
     * @param mask     Http Method Mask
     * @param path     regular expr
     * @param uses ctx-handlers array
     * @return Router
     * @see HttpMethod
     */
    ROUTER use(int mask, Pattern path, String rawDef, String[] inUrlParams, CtxHandler[] uses);

//    <A> ROUTER any(int mask,
//                   Pattern pattern,
//                   String[] inUrlParams,
//                   CtxHandler[] filters,
//                   Function<Request.Param<A>, HttpCtx> any,
//                   Callback<A> callback);
//
//    <A, B> ROUTER any(int mask, Pattern pattern, String[] inUrlParams, CtxHandler[] filters,
//                      Function<Tuple<Request.Param<A>, Request.Param<B>>, HttpCtx> any, Callback.C2<A, B> callback);
//
//    <A, B, C> ROUTER any(int mask, Pattern pattern, String[] inUrlParams, CtxHandler[] filters,
//                         Function<Tuple.T3<Request.Param<A>, Request.Param<B>, Request.Param<C>>,
//                                 HttpCtx> any, Callback.C3<A, B, C> callback);
//
//    <A, B, C, D> ROUTER any(int mask, Pattern pattern, String[] inUrlParams, CtxHandler[] filters,
//                            Function<Tuple.T4<Request.Param<A>, Request.Param<B>, Request.Param<C>, Request.Param<D>>, HttpCtx> any,
//                            Callback.C4<A, B, C, D> callback);
//
//    <A, B, C, D, E4> ROUTER any(int mask, Pattern pattern, String[] inUrlParams, CtxHandler[] filters,
//                               Function<Tuple.T5<Request.Param<A>,Request.Param<B>,Request.Param<C>,Request.Param<D>,Request.Param<E4>>, HttpCtx> any,
//                               Callback.C5<A,B,C,D,E4> callback);

    default ROUTER use(int mask, Pattern path, String rawDef, String[] inUrlParams, Mid... mids) {
        return use(mask, path, rawDef, inUrlParams, S._for(mids).map(Mid::toCtxHandler).joinArray(new CtxHandler[0]));
    }

    default ROUTER use(int mask, String path, Mid... mids) {
        PreCompiledPath preCompiledPath = compiler.compile(path);
        return use(mask, preCompiledPath.pattern, path, preCompiledPath.names, mids);
    }

    default ROUTER use(int mask, String path, CtxHandler... mids) {
        PreCompiledPath preCompiledPath = compiler.compile(path);
        return use(mask, preCompiledPath.pattern, path, preCompiledPath.names, mids);
    }

    default ROUTER use(CtxHandler... mids) {
        return use(HttpMethod.maskAll(), all_through, "/*", empty_params, mids);
    }

    default ROUTER use(Mid... mids) {
        return use(HttpMethod.maskAll(), all_through, "/*", empty_params, mids);
    }

    default ROUTER use(String path, Mid... mids) {
        return use(HttpMethod.maskAll(), path, mids);
    }

    default ROUTER get(String path, Mid... mids) {
        return use(HttpMethod.mask(HttpMethod.GET), path, mids);
    }

    default ROUTER post(String path, Mid... mids) {
        return use(HttpMethod.mask(HttpMethod.POST), path, mids);
    }

    default ROUTER del(String path, Mid... mids) {
        return use(HttpMethod.mask(HttpMethod.DELETE), path, mids);
    }

    default ROUTER put(String path, Mid... mids) {
        return use(HttpMethod.mask(HttpMethod.PUT), path, mids);
    }

    default ROUTER use(String path, CtxHandler... mids) {
        return use(HttpMethod.maskAll(), path, mids);
    }

    default ROUTER get(String path, CtxHandler... mids) {
        return use(HttpMethod.mask(HttpMethod.GET), path, mids);
    }

    default ROUTER post(String path, CtxHandler... mids) {
        return use(HttpMethod.mask(HttpMethod.POST), path, mids);
    }

    default ROUTER del(String path, CtxHandler... mids) {
        return use(HttpMethod.mask(HttpMethod.DELETE), path, mids);
    }

    default ROUTER put(String path, CtxHandler... mids) {
        return use(HttpMethod.mask(HttpMethod.PUT), path, mids);
    }

    /**
     * default call
     * THIS WILL BE CALLED IF NONE OF ABOVE MIDS FINISHED THE PROCESSING
     */
    ROUTER otherwise(CtxHandler... mids);

    default ROUTER otherwise(Mid... mids) {
        return otherwise(S._for(mids).map(Mid::toCtxHandler).joinArray(CtxHandler.EMPTY_ARRAY));
    }

    static String sanitisePath(String... path){
        return String.join("/", S._for(path).map(p ->{
            var ret = p.replaceAll("(/)+", "/");
            if(!p.startsWith("/")) {
                ret = "/" +p;
            }
            if(ret.endsWith("/") && ret.length() > 1) {
                ret = ret.substring(0, ret.length() -1);
            }
            return ret;
        })).replaceAll("(/)+", "/");
    }
}
