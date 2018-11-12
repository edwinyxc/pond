package pond.core;

import javax.swing.text.html.HTMLDocument;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CtxBase implements Context {

    private volatile AtomicInteger index = new AtomicInteger(0);
    //hold all properties
    private final LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
    //hold all services
//    private final HashMap<String, Service> services = new HashMap<>();
    private final List<CtxHandler> jobs = new LinkedList<>();
    private final List<Throwable> errors = new LinkedList<>();


    @Override
    public LinkedHashMap<String, Object> properties(){
        return properties;
    }

    @Override
    public void insert(CtxHandler h) {
        var j = (LinkedList<CtxHandler>) jobs;
        if(index.get() + 1 <= jobs.size()){
            j.add(index.get() + 1, h);
        }
    }

//    @Override
//    public HashMap<String, Service> services() {
//        return services;
//    }

    @Override
    public List<CtxHandler> jobs() {
        return jobs;
    }

    @Override
    public CtxHandler next() {
        int cur = index.get();
        if(cur + 1>= jobs.size()) return null;
        return jobs.get(index.addAndGet(1));
    }

    @Override
    public CtxHandler current() {
        int curId = index.get();
        if(curId >= jobs.size()) return null;
        return jobs.get(curId);
    }

    @Override
    public void error(Throwable a) {
        errors.add(a);
    }

    @Override
    public void removeRest() {
        Iterator<CtxHandler> iter = jobs.iterator();
        boolean needRemove = false;
        for(; iter.hasNext(); ){
            var ctx = iter.next();
            if(needRemove){
                iter.remove();
            }
            if(ctx == current()){
                needRemove = true;
            }
        }
    }

    @Override
    public List<Throwable> errors() {
        return errors;
    }

    @Override
    public Iterator<CtxHandler> iterator() {
        return jobs.iterator();
    }
}
