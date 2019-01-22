package pond.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.f.Callback;
import pond.common.f.Function;

public interface CtxHandler<T extends Ctx> extends Callback<T> {

    Logger logger = LoggerFactory.getLogger(CtxHandler.class);
    CtxHandler NOOP = ctx -> {};
    CtxHandler[] EMPTY_ARRAY = new CtxHandler[0];

    interface Flow<T extends Ctx> extends CtxHandler<T> {
        CtxFlowProcessor targetSubscriber();
    }

    static <A> CtxHandler<? extends Ctx> provide(Entry<A> entry, A t){
        return ctx ->  ctx.set(entry, t);
    }

    static <A> CtxHandler<? extends Ctx> provider(Entry<A> entry, Function.F0<A> t){
        return ctx -> { ctx.set(entry, t.apply()); };
    }


    static < A, B, C, D, E, F, G> CtxHandler<? extends Ctx> consume(
        Entry<A> A,
        Entry<B> B,
        Entry<C> C,
        Entry<D> D,
        Entry<E> E,
        Entry<F> F,
        Entry<G> G,
        Callback.C7<A, B, C, D, E, F, G> consumer ){
        return ctx -> {
            consumer.apply(
                ctx.get(A),
                ctx.get(B),
                ctx.get(C),
                ctx.get(D),
                ctx.get(E),
                ctx.get(F),
                ctx.get(G)
            );
        };
    }

    static <A, B, C, D, E, F> CtxHandler<? extends Ctx> consume(
        Entry<A> A,
        Entry<B> B,
        Entry<C> C,
        Entry<D> D,
        Entry<E> E,
        Entry<F> F,
        C6<A, B, C, D, E, F> consumer) {
        return ctx -> {
            consumer.apply(
                ctx.get(A),
                ctx.get(B),
                ctx.get(C),
                ctx.get(D),
                ctx.get(E),
                ctx.get(F)
            );
        };
    }

    static <A, B, C, D, E> CtxHandler<? extends Ctx> consume(
        Entry<A> A,
        Entry<B> B,
        Entry<C> C,
        Entry<D> D,
        Entry<E> E,
        Callback.C5<A, B, C, D, E> consumer) {
        return ctx -> {
            consumer.apply(
                ctx.get(A),
                ctx.get(B),
                ctx.get(C),
                ctx.get(D),
                ctx.get(E)
            );
        };
    }

    static <A, B, C, D> CtxHandler<? extends Ctx> consume(
        Entry<A> A,
        Entry<B> B,
        Entry<C> C,
        Entry<D> D,
        C4<A, B, C, D> consumer ) {
        return ctx -> {
             consumer.apply(
                ctx.get(A),
                ctx.get(B),
                ctx.get(C),
                ctx.get(D)
            );
        };
    }

    static <A, B, C> CtxHandler<? extends Ctx> consume(
        Entry<A> A,
        Entry<B> B,
        Entry<C> C,
        C3<A, B, C> consumer) {
        return ctx -> {
            consumer.apply(ctx.get(A), ctx.get(B), ctx.get(C));
        };
    }

    static <A, B> CtxHandler<? extends Ctx> consume(
        Entry<A> A,
        Entry<B> B,
        C2<A, B> consumer) {
        return ctx -> {
            consumer.apply(ctx.get(A), ctx.get(B));
        };
    }

    static <A> CtxHandler<? extends Ctx> consume(
        Entry<A> entry,
        Callback<A> consumer
    ){
        return ctx -> {
            consumer.apply(ctx.get(entry));
        };
    }

    //
static <R, A, B, C, D, E, F, G> CtxHandler<? extends Ctx> process(
        Entry<A> A,
        Entry<B> B,
        Entry<C> C,
        Entry<D> D,
        Entry<E> E,
        Entry<F> F,
        Entry<G> G,
        Function.F7<R, A, B, C, D, E, F, G> processor
       ) {
        return ctx -> {
            ctx.set(Ctx.LAST_RESULT, processor.apply(
                ctx.get(A),
                ctx.get(B),
                ctx.get(C),
                ctx.get(D),
                ctx.get(E),
                ctx.get(F),
                ctx.get(G)
            ));
        };
    }
    static <R, A, B, C, D, E, F, G> CtxHandler<? extends Ctx> process(
        Entry<A> A,
        Entry<B> B,
        Entry<C> C,
        Entry<D> D,
        Entry<E> E,
        Entry<F> F,
        Entry<G> G,
        Function.F7<R, A, B, C, D, E, F, G> processor, Entry<R> endpoint) {
        return ctx -> {
            ctx.set(endpoint, processor.apply(
                ctx.get(A),
                ctx.get(B),
                ctx.get(C),
                ctx.get(D),
                ctx.get(E),
                ctx.get(F),
                ctx.get(G)
            ));
        };
    }
  static <R, A, B, C, D, E, F> CtxHandler<? extends Ctx> process(
        Entry<A> A,
        Entry<B> B,
        Entry<C> C,
        Entry<D> D,
        Entry<E> E,
        Entry<F> F,
        Function.F6<R, A, B, C, D, E, F> processor, Entry<R> endpoint) {
        return ctx -> {
            ctx.set(endpoint, processor.apply(
                ctx.get(A),
                ctx.get(B),
                ctx.get(C),
                ctx.get(D),
                ctx.get(E),
                ctx.get(F)
            ));
        };
    }

    static <R, A, B, C, D, E> CtxHandler<? extends Ctx> process(
        Entry<A> A,
        Entry<B> B,
        Entry<C> C,
        Entry<D> D,
        Entry<E> E,
        Function.F5<R, A, B, C, D, E> processor
    ) {
        return ctx -> {
            ctx.set(Ctx.LAST_RESULT, processor.apply(
                ctx.get(A),
                ctx.get(B),
                ctx.get(C),
                ctx.get(D),
                ctx.get(E)
            ));
        };
    }
    static <R, A, B, C, D, E> CtxHandler<? extends Ctx> process(
        Entry<A> A,
        Entry<B> B,
        Entry<C> C,
        Entry<D> D,
        Entry<E> E,
        Function.F5<R, A, B, C, D, E> processor, Entry<R> endpoint) {
        return ctx -> {
            ctx.set(endpoint, processor.apply(
                ctx.get(A),
                ctx.get(B),
                ctx.get(C),
                ctx.get(D),
                ctx.get(E)
            ));
        };
    }

    static <R, A, B, C, D> CtxHandler<? extends Ctx> process(
        Entry<A> A,
        Entry<B> B,
        Entry<C> C,
        Entry<D> D,
        Function.F4<R, A, B, C, D> processor
    ) {
        return ctx -> {
            ctx.set(Ctx.LAST_RESULT, processor.apply(
                ctx.get(A),
                ctx.get(B),
                ctx.get(C),
                ctx.get(D)
            ));
        };
    }
    static <R, A, B, C, D> CtxHandler<? extends Ctx> process(
        Entry<A> A,
        Entry<B> B,
        Entry<C> C,
        Entry<D> D,
        Function.F4<R, A, B, C, D> processor, Entry<R> endpoint) {
        return ctx -> {
            ctx.set(endpoint, processor.apply(
                ctx.get(A),
                ctx.get(B),
                ctx.get(C),
                ctx.get(D)
            ));
        };
    }

    static <R, A, B, C> CtxHandler<? extends Ctx> process(
        Entry<A> A,
        Entry<B> B,
        Entry<C> C,
        Function.F3<R, A, B, C> processor
    ) {
        return ctx -> {
            ctx.set(Ctx.LAST_RESULT, processor.apply(ctx.get(A), ctx.get(B), ctx.get(C)));
        };
    }

    static <R, A, B, C> CtxHandler<? extends Ctx> process(
        Entry<A> A,
        Entry<B> B,
        Entry<C> C,
        Function.F3<R, A, B, C> processor,
        Entry<R> endpoint
    ) {
        return ctx -> {
            ctx.set(endpoint, processor.apply(ctx.get(A), ctx.get(B), ctx.get(C)));
        };
    }

    static <R, A, B> CtxHandler<? extends Ctx> process(
        Entry<A> A,
        Entry<B> B,
        Function.F2<R, A, B> processor
    ) {
        return ctx -> {
            ctx.set(Ctx.LAST_RESULT, processor.apply(ctx.get(A), ctx.get(B)));
        };
    }
    static <R, A, B> CtxHandler<? extends Ctx> process(
        Entry<A> A,
        Entry<B> B,
        Function.F2<R, A, B> processor, Entry<R> endpoint) {
        return ctx -> {
            ctx.set(endpoint, processor.apply(ctx.get(A), ctx.get(B)));
        };
    }

    static <R, A> CtxHandler<? extends Ctx> process(
        Entry<A> entry,
        Function<R, A> processor, Entry<R> endpoint) {
        return ctx -> {
            if(!ctx.contains(entry)){
                ctx.logger.warn("Taking a empty Entry:"+ entry.name()+"");
            }
            ctx.set(endpoint, processor.apply(ctx.get(entry)));
        };
    }
    static <R, A> CtxHandler<? extends Ctx> process(
        Entry<A> entry,
        Function<R, A> processor){
        return ctx -> {
            if(!ctx.contains(entry)){
                ctx.logger.warn("Taking a empty Entry:"+ entry.name()+"");
            }
            ctx.set(Ctx.LAST_RESULT, processor.apply(ctx.get(entry)));
        };
    }

    //default CtxHandler<T> flowToSelf() { }

    default CtxHandler<T> flowTo(CtxFlowProcessor target) {
        var _this = this;
        return new Flow<>() {

            @Override
            public void apply(T t) {
                _this.apply(t);
            }

            @Override
            public CtxFlowProcessor targetSubscriber() {
                return target;
            }

            @Override
            public String toString(){
                return "Flow["+hashCode()+ "::=>" + target.name() + "]";
            }

        };
    }

    static <T extends Ctx> CtxHandler<T> of(Callback<T> callback){
        return callback::apply;
    }

}
