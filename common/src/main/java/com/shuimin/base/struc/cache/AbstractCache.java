package com.shuimin.base.struc.cache;

import com.shuimin.base.S;
import com.shuimin.base.f.Callback;
import com.shuimin.base.f.Function;
import com.shuimin.base.struc.Cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractCache<K, V> extends Cache<K, V> {

    @SuppressWarnings("rawtypes")
    protected final Map cache;

    protected Function<V, K> onNothingFound = a -> null;

    @Override
    public Cache<K, V> onNotFound(Function<V, K> nothingFoundListener) {
        onNothingFound = S._notNull(nothingFoundListener);
        return this;
    }

    protected Callback._2<K, V> onRemove = (k, v) -> {
        // do nothing
    };

    @Override
    public Cache<K, V> onRemove(Callback._2<K, V> removeListener) {
        onRemove = S._notNull(removeListener);
        return this;
    }

    protected AbstractCache(Map<K, ?> a) {
        cache = a;
    }

    @Override
    public V get(K key) {
        synchronized (cache) {
            final V ret = _get(key);
            return ret == null ? onNothingFound.apply(key) : ret;
        }
    }

    protected abstract V _get(K key);

    @Override
    public V get(K key, Function<V, V> doWithVal) {
        synchronized (cache) {
            return doWithVal.apply(get(key));
        }
    }

    @Override
    public Cache<K, V> put(K key, V val) {
        synchronized (cache) {
            _put(key, val);
        }
        return this;
    }

    protected abstract void _put(K key, V val);

    @SuppressWarnings("unchecked")
    @Override
    public ConcurrentMap<K, V> asMap() {
        return new ConcurrentHashMap<K, V>(cache);
    }

    @Override
    public Cache<K, V> remove(K key) {
        synchronized (cache) {
            onRemove.apply(key, _get(key));
            cache.remove(key);
            return this;
        }
    }

    @Override
    public Cache<K, V> putAll(Map<K, V> m) {
        synchronized (cache) {
            S._for(m).each((t) -> _put(t.getKey(), t.getValue()));
        }
        return this;
    }

    @Override
    public Cache<K, V> removeAll(Iterable<K> keys) {
        synchronized (cache) {
            for (K k : keys) {
                this.remove(k);
            }
            return this;
        }
    }
}
