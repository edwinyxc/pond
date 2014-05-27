package com.shuimin.common.struc;

import com.shuimin.common.f.Callback;
import com.shuimin.common.f.Function;
import com.shuimin.common.struc.cache.DefaultCache;
import com.shuimin.common.struc.cache.FixedCache;
import com.shuimin.common.struc.cache.LRUCache;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public abstract class Cache<K, V> {

    /**
     * <p>
     * Get from cache, if not found, trigger doWhenNothing
     * </p>
     *
     * @param key
     * @return
     */
    public abstract V get(K key);

    /**
     * <p>
     * Get from cache, if not found, trigger doWhenNothing
     * </p>
     *
     * @param key
     * @param doWhenEmpty
     * @return
     */
    public abstract V get(K key, Function<V, V> doWhenEmpty);

    /**
     * <p>
     * Raw put
     * </p>
     *
     * @param key
     * @param val
     * @return
     */
    public abstract Cache<K, V> put(K key, V val);

    public abstract Cache<K, V> putAll(Map<K, V> m);

    public abstract Cache<K, V> remove(K key);

    public abstract Cache<K, V> removeAll(Iterable<K> key);

    public abstract ConcurrentMap<K, V> asMap();

    public abstract Cache<K, V> onNotFound(Function<V, K> nothingFoundLisener);

    public abstract Cache<K, V> onRemove(Callback.C2<K, V> removeListener);

    public static <K, V> Cache<K, V> lruCache(int max) {
        return new LRUCache<>(max);
    }

    public static <K, V> Cache<K, V> fixedCache(int max) {
        return new FixedCache<>(max);
    }

    public static <K, V> Cache<K, V> defaultCache() {
        return new DefaultCache<>();
    }
}
