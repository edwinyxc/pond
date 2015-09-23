package pond.common.struc.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.S;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;


public final class OnTimeExpiredCache<K, V> extends AbstractCache<K, V> {

  public long expireTime;
  public long checkInterval;
  //volatile boolean stop = false;
  Logger logger = LoggerFactory.getLogger(OnTimeExpiredCache.class);

  public OnTimeExpiredCache(long expireMs, long checkIntervalMs) {
    super(new ConcurrentHashMap<K, V>());
    expireTime = expireMs;
    checkInterval = checkIntervalMs;
    TimerTask task = new TimerTask() {
      @Override
      public void run() {

        Iterator it = OnTimeExpiredCache.this.cache.entrySet().iterator();

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
      }
    };
    Timer timer = new Timer();
    timer.scheduleAtFixedRate(task,0,checkInterval);
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
