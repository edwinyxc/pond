package pond.web;

import pond.common.S;

import java.util.Collections;
import java.util.List;

/**
 * Created by ed on 3/5/17.
 */

public class WellDefinedHandler implements CtxHandler {

    final List<ParamDef> def;
    final List<ResultDef> resultDefs;
    final CtxHandler handler;

    WellDefinedHandler(List<ParamDef> defs, CtxHandler handler) {
        this.def = defs;
        this.resultDefs = Collections.emptyList();
        this.handler = handler;
    }

    <R> WellDefinedHandler(List<ParamDef> defs, List<ResultDef> results, CtxHandler handler) {
        this.def = defs;
        this.resultDefs = results;
        this.handler = handler;
    }

//    <A, B> WellDefinedHandler(List<ParamDef> defs, Function<Either<A, B>, Ctx> handler,
//                              ResultDef<A> result_a,
//                              ResultDef<B> result_b
//    ) {
//        this.any = defs;
//        this.resultDefs = S.array(result_a, result_b);
//        this.handler = ctx -> handler.apply(ctx).match(
//                a -> result_a.apply(ctx, a),
//                b -> result_b.apply(ctx, b)
//        );
//    }
//
//    <A, B, C> WellDefinedHandler(List<ParamDef> defs, Function<Either.E3<A, B, C>, Ctx> handler,
//                                 ResultDef<A> result_a,
//                                 ResultDef<B> result_b,
//                                 ResultDef<C> result_c
//    ) {
//        this.any = defs;
//        this.resultDefs = S.array(result_a, result_b);
//        this.handler = ctx -> handler.apply(ctx).match(
//                a -> result_a.apply(ctx, a),
//                b -> result_b.apply(ctx, b),
//                c -> result_c.apply(ctx, c)
//        );
//    }
//
//    <A, B, C, D> WellDefinedHandler(List<ParamDef> defs, Function<Either.E4<A, B, C, D>, Ctx> handler,
//                                    ResultDef<A> result_a,
//                                    ResultDef<B> result_b,
//                                    ResultDef<C> result_c,
//                                    ResultDef<D> result_d
//    ) {
//        this.any = defs;
//        this.resultDefs = S.array(result_a, result_b);
//        this.handler = ctx -> handler.apply(ctx).match(
//                a -> result_a.apply(ctx, a),
//                b -> result_b.apply(ctx, b),
//                c -> result_c.apply(ctx, c),
//                d -> result_d.apply(ctx, d)
//        );
//    }
//
//    <A, B, C, D, E> WellDefinedHandler(List<ParamDef> defs, Function<Either.E5<A, B, C, D, E>, Ctx> handler,
//                                    ResultDef<A> result_a,
//                                    ResultDef<B> result_b,
//                                    ResultDef<C> result_c,
//                                    ResultDef<D> result_d,
//                                    ResultDef<E> result_e
//    ) {
//        this.any = defs;
//        this.resultDefs = S.array(result_a, result_b);
//        this.handler = ctx -> handler.apply(ctx).match(
//                a -> result_a.apply(ctx, a),
//                b -> result_b.apply(ctx, b),
//                c -> result_c.apply(ctx, c),
//                d -> result_d.apply(ctx, d),
//                e -> result_e.apply(ctx, e)
//        );
//    }

    void install(Route route) {
        S._for(def).each(route::addRequestParamNameAndTypes);
    }

    @Override
    public void apply(Ctx t) {
        handler.apply(t);
    }

}

