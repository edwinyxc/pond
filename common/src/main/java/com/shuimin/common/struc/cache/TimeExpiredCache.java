package com.shuimin.common.struc.cache;

import com.shuimin.common.S;


public final class TimeExpiredCache<K, V> extends AbstractCache<K, V> {

    public long expireTime;

    public TimeExpiredCache(int maxEntries, long expireMs) {
        super(new FixedSizeLinkedHashMap<K, VWithTime>(maxEntries));
        expireTime = expireMs;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected V _get(K key) {
        final VWithTime result = (VWithTime) cache.get(key);
        if (result.needExpire(expireTime)) {
            this.remove(key);
            return null;
        }
        return result.v;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void _put(K key, V val) {
        cache.put(key, new VWithTime(val, S.time()));
    }

    class VWithTime {
        public final V v;
        public final long putInTime;

        public VWithTime(V v, long time) {
            this.v = v;
            this.putInTime = time;
        }

        public boolean needExpire(long interval) {
            return interval < (S.time() - putInTime);
        }
    }
}
