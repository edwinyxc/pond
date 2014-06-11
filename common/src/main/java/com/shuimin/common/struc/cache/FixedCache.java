package com.shuimin.common.struc.cache;


public final class FixedCache<K, V> extends AbstractCache<K, V> {

    public FixedCache(int maxEntries) {
        super(new FixedSizeLinkedHashMap<K, V>(maxEntries));
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
