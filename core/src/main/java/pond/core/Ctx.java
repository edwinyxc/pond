package pond.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.S;
import pond.common.f.Function;
import pond.common.f.Tuple;

import java.util.*;
import java.util.concurrent.SubmissionPublisher;

import static pond.common.f.Tuple.pair;

/**
 *
 * <p>A Context delegation with a lot of helpful methods </p>
 * <p>
 *     Stateful Context for Executive-Continuation
 * </p>
 * @see Ctx#runReactiveFlow(ReactiveFlowConfig)
 * @see Ctx#bind()
 * @see Entry
 * @see Context
 * @see Ctx#getEntry(Entry)
 * @since java11
 */
@FunctionalInterface
public interface Ctx {

    Logger logger = LoggerFactory.getLogger(Ctx.class);
    Entry<CtxFlowProcessor> CtxFlowProcessor = new Entry<>(Ctx.class, "CtxFlowProcessor");

    Entry<? extends Ctx> SELF = new Entry<>(Ctx.class, "_self");
    Entry<Object> LAST_RESULT = new Entry<>("_LAST_RESULT");

    Context delegate();
    /**
     * Short hand for delegate. A functional interface is
     * capable to cast to any type of its derived interfaces
     * or their intersection types, typical usage:
     *  <p>
     *      //using functional delegation and intersection types
     *      {@code var a = (HttpCtx & Ctx & StaticFileServerCtx)ctx::bind}
     *  </p>
     * @return the delegated Context usually you don't need
     */
    default Context bind() {
        return delegate();
    }

    default CtxFlowProcessor flowProcessor(){
        var ret = this.getEntry(CtxFlowProcessor);
        if(ret == null){
            ret = new CtxFlowProcessor("Ctx@"+this.delegate().hashCode());
            this.set(CtxFlowProcessor, ret);
        }
        return ret;
    }

    default Thread currentThread() {
        return delegate().currentThread();
    }

    default List<CtxHandler> jobs() {
        return delegate().jobs();
    }

    default CtxHandler current() {
        return delegate().current();
    }

    default CtxHandler next() {
        return delegate().next();
    }

    default List<Throwable> errors() {
        return delegate().errors();
    }

    default LinkedHashMap<String, Object> properties() {
        return delegate().properties();
    }

    default void error(Throwable a) {
        delegate().error(a);
    }

    default void push(CtxHandler<? extends Ctx> ctxHandler) {
        jobs().add(ctxHandler);
    }
    default void pushAll(Iterable<CtxHandler<? extends Ctx>> executables) {
        S._for(executables).each(e -> jobs().add(e));
    }

    default boolean contains(Entry entry) {
        return this.properties().keySet().contains(entry.key);
    }

    default Ctx put(String k, Object v) {
        this.delegate().properties().put(k,v);
        return this;
    }

    @SuppressWarnings("unchecked")
    default<T> T get(String k) {
        return (T) this.delegate().properties().get(k);
    }

    @SuppressWarnings("unchecked")
    default <T> T getEntry(Entry<T> key) {
        if(key == SELF)  return (T) this;
        return (T) Optional.ofNullable(this.delegate().properties().get(key.key)).orElse(key.nil);
    }

    default <T> Ctx set(Entry<T> key, T value) {
        this.delegate().properties().put(key.key, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    default <T> T getOrDefault(Entry<T> key, T _default) {
        return (T) this.delegate().properties().getOrDefault(key.key, _default);
    }

    default <T> T getOrPutDefault(Entry<T> key, T _default) {
        if (this.getEntry(key) == null) this.set(key, _default);
        return this.getEntry(key);
    }

    default <T> T getOrSupplyDefault(Entry<T> key, Function.F0<T> supplier) {
        if (this.getEntry(key) == null) this.set(key, supplier.apply());
        return this.getEntry(key);
    }

    default void insert(CtxHandler<? extends Ctx> ctxHandler){
        delegate().insert(ctxHandler);
    }

    default void terminate() {
        insert(null);
    }

    /**
     * Run this Ctx in the same Thread by force
     */
    @SuppressWarnings("unchecked")
    default void run() {
        CtxHandler exec = current();

        for(;exec != null;exec = this.next()) {
            exec.apply(this);
        }
    }

    /**
     * <ul>
     * Run this Ctx in a Completion Reactive-Flow thus,
     * <li>
     * a) Each CtxHandler is converted to a CtxHandler.Flow targeted to Ctx.CtxFlowProcessor by default
     * except those has defined already (target to an external Subscriber)
     * </li>
     * <li>
     * b) Publish Ctx to CtxFlowProcessor defined in CtxHandler.Flow.target
     * </li>
     * <li>
     * c) Run executables synchronously in the current thread UNTIL that
     *    CtxHandler.Flow's target is pointed to another Subscriber.
     *    Then submit the Ctx to it in this case.
     *    The target subscriber MUST BE subscribed to this.flowProcessor and promise to submit Ctx back in same manner.
     *    Consider CtxFlowProcessor be the target as your first choice.
     * </li>
     * <li>
     * d) Completion is promised in this pattern, the final state is the Ctx itself.
     * </li>
     * <li>
     * PS: This is a Observable-Command combined pattern
     * <br/>
     * (Command -> CtxHandler; Observable -> CtxFlowProcessor(Ctx))
     * <br/>
     * <strong>All hail to ReactiveStreams!!!</strong>
     * </li>
     * </ul>
     */
    default void runReactiveFlow(ReactiveFlowConfig config) {
        //create a job publisher
        SubmissionPublisher<Ctx> temp_publisher = new SubmissionPublisher<>();
        CtxFlowProcessor processor = this.flowProcessor();

        var cfg = config.build(this);

        S._for(cfg).each(t -> {
            var p = t._a;
            S._for(t._b).each(i -> {
                CtxHandler ctxHandler = this.jobs().get(i);
                jobs().set(i, ctxHandler.flowTo(p));
            });
        });
        S._for(jobs()).each(t -> {
            assert t instanceof CtxHandler.Flow;
        });

        //when
        temp_publisher.subscribe(processor);
        //dispatch jobs
        temp_publisher.submit(this);
        temp_publisher.close();
    }

    /**
     * Run Reactiveflow with default config
     *
     */
    default void runReactiveFlow() {
        runReactiveFlow(ReactiveFlowConfig.DEFAULT);
    }

    @FunctionalInterface
    interface ReactiveFlowConfig{
        List<Tuple<CtxFlowProcessor, List<Integer>>> build(Ctx ctx);

        ReactiveFlowConfig DEFAULT = ctx -> defaultTo(ctx.flowProcessor()).build(ctx);

        static ReactiveFlowConfig defaultTo(CtxFlowProcessor processor){
            return ctx -> {
                //convert all executable to Flow
                List<Integer> wild_executables =
                    S._for(ctx.jobs())
                        .map((e, i) -> pair(i, e))
                        .filter(t -> !(t._b instanceof CtxHandler.Flow))
                        .map(t -> t._a)
                        .toList();
                return new ArrayList<>(){{
                    add(pair(processor, wild_executables));
                }};
            };
        }
    }

    default void close() {
        this.getEntry(CtxFlowProcessor).close();
    }

}
