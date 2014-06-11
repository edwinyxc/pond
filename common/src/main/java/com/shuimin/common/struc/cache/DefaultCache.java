package com.shuimin.common.struc.cache;

import java.util.HashMap;


public final class DefaultCache<K, V> extends AbstractCache<K, V> {

    public DefaultCache() {
        super(new HashMap<K, V>());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected V _get(K key) {
        return (V) cache.get(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void _put(K key, V val) {
        cache.put(key, val);
    }

}
