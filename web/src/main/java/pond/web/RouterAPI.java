package pond.web;

import pond.common.S;
import pond.common.f.Callback;
import pond.common.f.Function;
import pond.common.f.Tuple;
import pond.web.http.HttpMethod;

import java.util.regex.Pattern;

/**
 * Router  API
 * Holder of responsibility chain, also called business chain
 *
 * @param <ROUTER>
 */
public interface RouterAPI<ROUTER extends Router> {

    PathToRegCompiler compiler = new ExpressPathToRegCompiler();

    Pattern all_through = Pattern.compile("/.*");
    String[] empty_params = new String[0];


    /**
     * Add a middleware to Router
     *
     * @param mask     Http Method Mask
     * @param path     regular expr
     * @param handlers ctx-handlers array
     * @return Router
     * @see pond.web.http.HttpMethod
     */
    ROUTER use(int mask, Pattern path, String[] inUrlParams, CtxHandler[] handlers);

//    <A> ROUTER def(int mask,
//                   Pattern pattern,
//                   String[] inUrlParams,
//                   CtxHandler[] filters,
//                   Function<Request.Param<A>, HttpCtx> def,
//                   Callback<A> callback);
//
//    <A, B> ROUTER def(int mask, Pattern pattern, String[] inUrlParams, CtxHandler[] filters,
//                      Function<Tuple<Request.Param<A>, Request.Param<B>>, HttpCtx> def, Callback.C2<A, B> callback);
//
//    <A, B, C> ROUTER def(int mask, Pattern pattern, String[] inUrlParams, CtxHandler[] filters,
//                         Function<Tuple.T3<Request.Param<A>, Request.Param<B>, Request.Param<C>>,
//                                 HttpCtx> def, Callback.C3<A, B, C> callback);
//
//    <A, B, C, D> ROUTER def(int mask, Pattern pattern, String[] inUrlParams, CtxHandler[] filters,
//                            Function<Tuple.T4<Request.Param<A>, Request.Param<B>, Request.Param<C>, Request.Param<D>>, HttpCtx> def,
//                            Callback.C4<A, B, C, D> callback);
//
//    <A, B, C, D, E> ROUTER def(int mask, Pattern pattern, String[] inUrlParams, CtxHandler[] filters,
//                               Function<Tuple.T5<Request.Param<A>,Request.Param<B>,Request.Param<C>,Request.Param<D>,Request.Param<E>>, HttpCtx> def,
//                               Callback.C5<A,B,C,D,E> callback);

    default ROUTER use(int mask, Pattern path, String[] inUrlParams, Mid... mids) {
        return use(mask, path, inUrlParams, S._for(mids).map(CtxHandler::mid).join());
    }

    default ROUTER use(int mask, String path, Mid... mids) {
        PreCompiledPath preCompiledPath = compiler.compile(path);
        return use(mask, preCompiledPath.pattern, preCompiledPath.names, mids);
    }

    default ROUTER use(int mask, String path, CtxHandler... mids) {
        PreCompiledPath preCompiledPath = compiler.compile(path);
        return use(mask, preCompiledPath.pattern, preCompiledPath.names, mids);
    }

    default ROUTER use(CtxHandler... mids) {
        return use(HttpMethod.maskAll(), all_through, empty_params, mids);
    }

    default ROUTER use(Mid... mids) {
        return use(HttpMethod.maskAll(), all_through, empty_params, mids);
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
        return otherwise(S._for(mids).map(CtxHandler::mid).join());
    }
}
