package com.shuimin.pond.codec.mvc.model;

import com.shuimin.common.f.Function;
import com.shuimin.pond.codec.mvc.Model;
import com.shuimin.pond.core.db.AbstractRecord;

import java.util.HashMap;
import java.util.Map;

import static com.shuimin.common.S._notNullElse;
import static com.shuimin.common.S.str.underscore;
import static com.shuimin.common.f.Function.F0;

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

    public class SimpleField<E> implements Field<E> {
        String name;

        public SimpleField(String name) {
            this.name = name;
            AbstractModel.this.declaredFields().add(name);
            AbstractModel.this.set(name,null);
        }

        @Override
        public Field init(F0<E> supplier) {
            AbstractModel.this.put(name,_notNullElse(supplier,()-> null).apply());
            return this;
        }

        @Override
        public Field onGet(Function<E,E> get) {
            AbstractModel.this.getters.put(name,get);
            return this;
        }

        @Override
        public Field onSet(Function<E,E> set) {
            AbstractModel.this.setters.put(name, set);
            return this;
        }
    }

    @Override
    public <E> Field<E> field(String name) {
        return new SimpleField<E>(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> E get(String s) {
        Function getter = getters.get(s);
        Object t = super.get(s);
        return getter != null ? (E) getter.apply(t) : (E) t;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Model set(String s, Object val) {
        Function setter = setters.get(s);
        if (setter != null)
            this.put(s, setter.apply(val));
        else
            this.put(s, val);
        return this;
    }
}
