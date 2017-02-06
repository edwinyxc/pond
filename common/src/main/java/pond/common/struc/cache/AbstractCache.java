package pond.common.struc.cache;

import pond.common.S;
import pond.common.f.Callback;
import pond.common.f.Function;
import pond.common.struc.Cache;

import java.util.Map;

public abstract class AbstractCache<K, V> extends Cache<K, V> {

  @SuppressWarnings("rawtypes")
  protected final Map cache;

  protected Function<V, K> onNothingFound = a -> null;
  protected Callback.C2<K, V> onRemove = (k, v) -> {
    // do nothing
  };

  protected AbstractCache(Map<K, ?> a) {
    cache = a;
  }

  @Override
  public Cache<K, V> onNotFound(Function<V, K> nothingFoundListener) {
    S._assert(nothingFoundListener);
    onNothingFound = nothingFoundListener;
    return this;
  }

  @Override
  public Cache<K, V> onRemove(Callback.C2<K, V> removeListener) {
    S._assert(removeListener);
    onRemove = removeListener;
    return this;
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
  @SuppressWarnings({"unchecked","rawtypes"})
  public V get(K key, Function<V, Cache> onNothingFound) {
    V ret = this._get(key);
    if( ret == null ) {
      ret = onNothingFound.apply(this);
      this.cache.put(key, ret);
    }
    return ret;
  }

  @Override
  public V get(K key, V cache_val) {
    return get(key, _cache -> cache_val);
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
  public Map<K, V> asMap() {
    return cache;
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
