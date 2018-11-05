package pond.core;

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

    default void push(Collection<Executable> executables){
        jobs().addAll(executables);
    }
}
