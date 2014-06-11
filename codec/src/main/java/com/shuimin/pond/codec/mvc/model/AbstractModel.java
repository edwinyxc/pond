package com.shuimin.pond.codec.mvc.model;

import com.shuimin.common.f.Function;
import com.shuimin.pond.core.db.AbstractRecord;
import com.shuimin.pond.codec.mvc.Model;

import java.util.HashMap;
import java.util.Map;

import static com.shuimin.common.S.str.underscore;

/**
 * Created by ed on 5/30/14.
 */
public class AbstractModel extends AbstractRecord
        implements Model {


    protected Map<String, Function> getters = new HashMap<>();

    protected Map<String, Function> setters = new HashMap<>();

    {
        //default by class name
        table(underscore(this.getClass().getSimpleName()));
    }


    @Override
    public Model field(String name, Function.F0 supplier) {
        this.put(name, supplier.apply());
        return this;
    }

    @Override
    public Model field(String name) {
        this.put(name, null);
        return this;
    }

    @Override
    public Model onSet(String s, Function converter) {
        this.setters.put(s, converter);
        return this;
    }

    @Override
    public Model onGet(String s, Function f) {
        this.getters.put(s, f);
        return this;
    }


    @Override
    @SuppressWarnings("unchecked")
    public <E> E get(String s) {
        Function getter = getters.get(s);
        Object t = super.get(s);
        return getter != null ? (E) getter.apply(t) : (E) t;
    }

    @Override
    public Model set(String s, Object val) {
        Function setter = setters.get(s);
        if (setter != null)
            this.put(s, setter.apply(val));
        else
            this.put(s, val);
        return this;
    }
}
