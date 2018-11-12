package pond.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.S;
import pond.common.f.Tuple;

import java.util.*;
import java.util.concurrent.SubmissionPublisher;

import static pond.common.f.Tuple.pair;

@FunctionalInterface
public interface Ctx {

    Logger logger = LoggerFactory.getLogger(Ctx.class);
    Entry<CtxFlowProcessor> CtxFlowProcessor = new Entry<>(Ctx.class, "CtxFlowProcessor");
    Entry<? extends Ctx> SELF = new Entry<>(Ctx.class, "_self");

    Context delegate();
    /**
     * Short hand for delegate
     * use like this var x = (Ctx & CtxHttp & CtxXXX.XXX)ctx::bind
     * @return
     */
    default Context bind() {
        return delegate();
    }

    default CtxFlowProcessor flowProcessor(){
        var ret = this.get(CtxFlowProcessor);
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
    default <T> T get(Entry<T> key) {
        if(key == SELF)  return (T) this;
        return (T) this.delegate().properties().get(key.key);
    }

    default <T> Ctx set(Entry<T> key, T value) {
        this.delegate().properties().put(key.key, value);
        return this;
    }

    default <T> T getLazy(Entry<T> key, T _default) {
        if (this.get(key) == null) this.set(key, _default);
        return this.get(key);
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
     *
     * Run this Ctx in a Completion Reactive-Flow thus,
     * a) Each CtxHandler is converted to a CtxHandler.Flow targeted to Ctx.CtxFlowProcessor by default
     *    except those has defined already (target to an external Subscriber)
     * b) Publish Ctx to CtxFlowProcessor defined in CtxHandler.Flow.target
     * c) Run executables synchronously in the current thread UNTIL that
     *    CtxHandler.Flow's target is pointed to another Subscriber.
     *    Then submit the Ctx to it in this case.
     *    The target subscriber MUST BE subscribed to this.flowProcessor and promise to submit Ctx back in same manner.
     *    Consider CtxFlowProcessor be the target as your first choice.
     * d) Completion is promised in this pattern, the final state is the Ctx itself.
     *
     * PS: This is a Observable-Command combined pattern (Command: CtxHandler; Observable: CtxFlowProcessor(Ctx))
     * All hail to ReactiveStreams!!!
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
        this.get(CtxFlowProcessor).close();
    }

    class Entry<T> {
        final String key;

        public String name(){
            return key;
        }

        public Entry(String key) {
            this.key = key;
        }

        public Entry(Class<? extends Ctx> cls, String key) {
            this.key = cls.getCanonicalName() + "." + key;
        }

        static Entry<Object> LAST_RESULT = new Entry<>("_LAST_RESULT");
    }

}
