package pond.core;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CtxBase implements Context {

    private volatile AtomicInteger  index = new AtomicInteger(0);
    //hold all properties
    private final LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
    //hold all services
    private final HashMap<String, Service> services = new HashMap<>();
    private final List<Executable> jobs = new LinkedList<>();
    private final List<Throwable> errors = new LinkedList<>();


    @Override
    public LinkedHashMap<String, Object> properties(){
        return properties;
    }

    @Override
    public HashMap<String, Service> services() {
        return services;
    }

    @Override
    public List<Executable> jobs() {
        return jobs;
    }

    @Override
    public Executable next() {
        int nextId = index.get();
        if(nextId >= jobs.size()) return null;
        return jobs.get(index.getAndAdd(1));
    }

    @Override
    public Executable peek() {
        return jobs.get(index.get());
    }

    @Override
    public void error(Throwable a) {
        errors.add(a);
    }

    @Override
    public List<Throwable> errors() {
        return errors;
    }

    @Override
    public Iterator<Executable> iterator() {
        return jobs.iterator();
    }
}
