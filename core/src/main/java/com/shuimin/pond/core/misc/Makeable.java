package com.shuimin.pond.core.misc;

/**
 * @author ed
 */
@SuppressWarnings("unchecked")
public interface Makeable<V> {

    public default Makeable make(Config<V> y) {
        y.config((V) this);
        return this;
    }
}
