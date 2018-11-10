package pond.core;

import pond.common.S;
import pond.common.f.Tuple;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

import static pond.common.f.Tuple.pair;

@FunctionalInterface
public interface Ctx {

    Context delegate();

    class Keys {
        static
        Entry<CtxFlowProcessor> CtxFlowProcessor = new Entry<>(Ctx.class, "CtxFlowProcessor");
    }

    default CtxFlowProcessor flowProcessor(){
        var ret = this.get(Keys.CtxFlowProcessor);
        if(ret == null){
            ret = new CtxFlowProcessor();
            this.set(Keys.CtxFlowProcessor, ret);
        }
        return ret;
    }

    default Thread currentThread() {
        return delegate().currentThread();
    }

    default List<Executable> jobs() {
        return delegate().jobs();
    }

    default Executable peek() {
        return delegate().peek();
    }

    default Executable next() {
        return delegate().next();
    }

    default List<Throwable> errors() {
        return delegate().errors();
    }

    default HashMap<String, Service> services() {
        return delegate().services();
    }

    default LinkedHashMap<String, Object> properties() {
        return delegate().properties();
    }

    default void error(Throwable a) {
        delegate().error(a);
    }

    default void push(Executable... executables) {
        push(S._for(executables));
    }

    default void push(Iterable<Executable> executables) {
        S._for(executables).each(e -> jobs().add(e));
    }

    @SuppressWarnings("unchecked")
    default <T> T get(Entry<T> key) {
        return (T) this.delegate().properties().get(key.key);
    }

    default <T> Ctx set(Entry<T> key, T value) {
        this.delegate().properties().put(key.key, value);
        return this;
    }

    /**
     * Run this Ctx in the same Thread by force
     */
    default void run() {
        Executable exec;
        while (null != (exec = this.next())) {
            exec.body().apply(this);
        }
    }

    /**
     *
     * Run this Ctx in a Reactive-Flow thus,
     * a) Each Executable is converted to a Executable.Flow targeted to Ctx.CtxFlowProcessor by default
     *    except those has defined already (target to an external Subscriber)
     * b) Publish Ctx to CtxFlowProcessor defined in Executable.Flow.target
     * c) Run executables synchronously in the current thread UNTIL that
     *    Executable.Flow's target is pointed to another Subscriber.
     *    Then submit the Ctx to it in this case.
     *    The target subscriber MUST BE subscribed to this.flowProcessor and promise to submit Ctx back in same manner.
     *    Consider CtxFlowProcessor be the target as your first choice.
     *
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
                Executable executable = this.jobs().get(i);
                jobs().set(i, executable.flowTo(p));
            });
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
        List<Tuple<Flow.Subscriber<Ctx>, List<Integer>>> build(Ctx ctx);

        ReactiveFlowConfig DEFAULT = ctx -> {
            //convert all executable to Flow
            List<Integer> wild_executables =
                S._for(ctx.jobs())
                    .map((e, i) -> pair(i, e))
                    .filter(t -> !(t._b instanceof Executable.Flow))
                    .map(t -> t._a)
                    .toList();
            return new ArrayList<>(){{
                add(pair(ctx.flowProcessor(), wild_executables));
            }};
        };
    }

    default void close() {
        this.get(Keys.CtxFlowProcessor).close();
    }

    class Entry<T> {
        final String key;

        public Entry(String key) {
            this.key = key;
        }

        public Entry(Class<? extends Ctx> cls, String key) {
            this.key = cls.getCanonicalName() + "." + key;
        }
    }

}
