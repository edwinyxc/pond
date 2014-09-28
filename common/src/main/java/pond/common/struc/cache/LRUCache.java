package pond.common.struc.cache;


public final class LRUCache<K, V> extends AbstractCache<K, V> {

    public LRUCache(int maxEntries) {
        super(new LRULinkedHashMap<K, V>(maxEntries));
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
