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
    public void terminate() {
        var j = (LinkedList<Executable>) jobs;
        if(index.get() + 1 <= jobs.size()){
            j.add(index.get() + 1, null);
        }
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
        int cur = index.get();
        if(cur + 1>= jobs.size()) return null;
        return jobs.get(index.addAndGet(1));
    }

    @Override
    public Executable current() {
        int curId = index.get();
        if(curId >= jobs.size()) return null;
        return jobs.get(curId);
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
