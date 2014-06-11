package com.shuimin.pond.codec.mvc;

import com.shuimin.common.S;
import com.shuimin.common.abs.Config;
import com.shuimin.common.f.Function;
import com.shuimin.common.f.Tuple;
import com.shuimin.pond.core.Middleware;
import com.shuimin.pond.core.http.HttpMethod;
import com.shuimin.pond.core.mw.Dispatcher;
import com.shuimin.pond.core.spi.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.shuimin.common.S._for;
import static com.shuimin.common.S.str.underscore;
import static com.shuimin.common.f.Tuple.t3;
import static com.shuimin.pond.core.http.HttpMethod.*;

/**
 * Created by ed on 5/26/14.
 */
public abstract class Controller implements Config<Dispatcher> {

    static Logger logger = Logger.createLogger(Controller.class);

    protected List<Tuple.T3<Integer, String, Middleware>> actions = new ArrayList<>();

    protected Function<String, Controller> nameSupplier =
            (res) -> underscore(this.getClass().getSimpleName());


    public Controller name(Function<String, Controller> func) {
        this.nameSupplier = func;
        return this;
    }

    public Controller name(String name) {
        this.nameSupplier = (r) -> name;
        return this;
    }

    @SuppressWarnings("unchecked")
    public <E extends Controller> E bind(int mask, String path, Middleware... mw) {
        actions.add(t3(mask, path, Middleware.string(Arrays.asList(mw))));
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public <E extends Controller> E get(String path, Middleware... mw) {
        actions.add(t3(HttpMethod.mask(GET), path, Middleware.string(Arrays.asList(mw))));
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public <E extends Controller> E put(String path, Middleware... mw) {
        actions.add(t3(HttpMethod.mask(PUT), path, Middleware.string(Arrays.asList(mw))));
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public <E extends Controller> E post(String path, Middleware... mw) {
        actions.add(t3(HttpMethod.mask(POST), path, Middleware.string(Arrays.asList(mw))));
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public <E extends Controller> E delete(String path, Middleware... mw) {
        actions.add(t3(HttpMethod.mask(DELETE), path, Middleware.string(Arrays.asList(mw))));
        return (E) this;
    }


    @Override
    public void config(Dispatcher dispatcher) {

        logger.debug("Binding resource controller : " + nameSupplier.apply(this));

        String namespace = "/" + nameSupplier.apply(this);
        _for(actions).each(t -> {
            //TODO debug not functioning
            logger.debug("binding " + t._c + "to " + namespace + t._b);
            dispatcher.bind(t._a, namespace + t._b, t._c);
        });
    }

    @Override
    public String toString() {
        return "Controller{" +
                "actions=" + S.dump(actions) +
                ", name=" + nameSupplier.apply(this) +
                '}';
    }
}
