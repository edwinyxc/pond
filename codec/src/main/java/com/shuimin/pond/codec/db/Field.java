package com.shuimin.pond.codec.db;

import com.shuimin.common.f.Function;

/**
 * Created by ed on 14-5-21.
 */
public class Field<T> {

    final String name;
    Function.F0<T> _default = () -> null;
    boolean isPri = false;

    public Field(String name) {
        this(name,()-> (T)"");
    }


    public Field(String name, Function.F0<T> provider) {
        this.name = name;
        this._default = provider;
    }
    public String name(){return name;}

    public Field defaultVal(T t) {
        _default = () -> t;
        return this;
    }

    public Field defaultVal(Function.F0<T> provider) {
        _default = provider;
        return this;
    }

    public T getDefault() {
        return _default.apply();
    }

    public Field PK() {
        this.isPri = true;
        return this;
    }


    public boolean isPrimaryKey(){
        return isPri;
    }
}
