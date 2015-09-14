package pond.common.struc.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.S;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public final class OnTimeExpiredCache<K, V> extends AbstractCache<K, V> {

  public long expireTime;
  public long checkInterval;
  volatile boolean stop = false;
  Logger logger = LoggerFactory.getLogger(OnTimeExpiredCache.class);

  public OnTimeExpiredCache(long expireMs, long checkIntervalMs) {
    super(new ConcurrentHashMap<K, V>());
    expireTime = expireMs;
    checkInterval = checkIntervalMs;

    new Thread(() -> {
      while (!stop) {
        try {
          Thread.sleep(checkInterval);
          Iterator it = this.cache.entrySet().iterator();

          S._debug(logger, log -> {
            log.debug("trigger clean");
          });

          while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            VWithTime vWithTime = (VWithTime) entry.getValue();
            if (vWithTime.needExpire(expireTime)) {

              S._debug(logger, log -> {
                log.debug("cleaning expired entry : " + entry);
              });

              it.remove();
            }
          }

        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }, "TimeExpireCache-daemon").start();
  }


  @SuppressWarnings("unchecked")
  @Override
  protected V _get(K key) {
    final VWithTime result = (VWithTime) cache.get(key);
    return result == null ? null : result.v;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void _put(K key, V val) {
    cache.put(key, new VWithTime(val, S.now()));
  }

  public void releaseDaemon() throws IOException {
    this.stop = true;
  }

  class VWithTime {
    public final V v;
    public final long putInTime;

    public VWithTime(V v, long time) {
      this.v = v;
      this.putInTime = time;
    }

    public boolean needExpire(long interval) {
      return interval < (S.now() - putInTime);
    }
  }

}
