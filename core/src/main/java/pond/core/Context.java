package pond.core;

import java.io.Serializable;
import java.util.*;


public interface Context extends Serializable, Iterable<Executable>{
    default Thread currentThread(){
        return Thread.currentThread();
    };
    List<Executable> jobs();
    Executable next();
    Executable current();
    List<Throwable> errors();
    HashMap<String, Service> services();
    LinkedHashMap<String, Object> properties();
    void terminate();
    void error(Throwable a);

    /*
    default void push(Executable... executables){
        jobs().addAll(Arrays.asList(executables));
    }

    default void push(Collection<Executable> executables){
        jobs().addAll(executables);
    }
    */

}
