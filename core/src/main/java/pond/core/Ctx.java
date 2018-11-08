package pond.core;

import pond.common.S;
import pond.common.f.Tuple;

import java.util.*;

@FunctionalInterface
public interface Ctx {
    Context delegate();

    default Thread currentThread(){
        return delegate().currentThread();
    }
    default List<Executable> jobs(){
        return delegate().jobs();
    }

    default Executable peek(){
        return delegate().peek();
    }

    default Executable next() {
        return delegate().next();
    }
    default List<Throwable> errors(){
        return delegate().errors();
    }

    default HashMap<String, Service> services(){
        return delegate().services();
    }

    default LinkedHashMap<String, Object> properties(){
        return delegate().properties();
    }


    default void error(Throwable a){
        delegate().error(a);
    }

    default void push(Executable... executables){
        jobs().addAll(Arrays.asList(executables));
    }

    default void push(Iterable<Executable> executables){
        S._for(executables).each(e -> jobs().add(e));
    }

    @SuppressWarnings("unchecked")
    default <T> T get(Entry<T> key){
        return (T) this.delegate().properties().get(key.key);
    }

    default <T> Ctx set(Entry<T> key, T value) {
        this.delegate().properties().put(key.key, value);
        return this;
    }

    /**
     * Run this Ctx in the same Thread
     */
    default void run(){
        Executable exec;
        while(null != (exec = this.next())){
            exec.body().apply(this);
        }
    }

    class Entry<T> {
        final String key;
        public Entry(String key){
            this.key = key;
        }

        public Entry(Class<? extends Ctx> cls, String key){
            this.key = cls.getCanonicalName() + "." + key;
        }
    }

}
