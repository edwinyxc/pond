package pond.core;

import java.io.Serializable;
import java.util.*;


public interface Context extends Serializable, Iterable<CtxHandler>{
    default Thread currentThread(){
        return Thread.currentThread();
    };
    List<CtxHandler> jobs();
    CtxHandler next();
    CtxHandler current();
    List<Throwable> errors();
    LinkedHashMap<String, Object> properties();
    void insert(CtxHandler ctxHandler);
    void error(Throwable a);
    void removeRest();

    /*
    default void push(CtxHandler... executables){
        jobs().addAll(Arrays.asList(executables));
    }

    default void push(Collection<CtxHandler> executables){
        jobs().addAll(executables);
    }
    */

}
