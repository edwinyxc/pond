package com.shuimin.base.struc;

import com.shuimin.base.f.Callback;
import com.shuimin.base.f.Function;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.shuimin.base.struc.cache.DefaultCache;
import com.shuimin.base.struc.cache.FixedCache;
import com.shuimin.base.struc.cache.LRUCache;

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

    public abstract Cache<K, V> onRemove(Callback._2<K, V> removeListener);

    public static <K, V> Cache<K, V> lruCache(int max) {
        return new LRUCache<K, V>(max);
    }

    public static <K, V> Cache<K, V> fixedCache(int max) {
        return new FixedCache<K, V>(max);
    }

    public static <K, V> Cache<K, V> defaultCache() {
        return new DefaultCache<K, V>();
    }
}
