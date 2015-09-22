package pond.common.struc;

import org.junit.Test;
import pond.common.S;
import pond.common.struc.cache.OnTimeExpiredCache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by ed on 9/9/15.
 */
public class OnTimeExpiredCacheTest {

  @Test
  public void test() throws InterruptedException {

//    S._debug_on(TimeExpiredCache.class);
    OnTimeExpiredCache<String, String> cache = new OnTimeExpiredCache<>(40, 5);

    for (int i = 0; i < 100; i++) {
      cache.put(String.valueOf(i), String.valueOf(i) + "_str");
    }

    assertEquals("12_str", cache.get("12"));
    Thread.sleep(60);
    S.echo(cache.get("12"));
    assertNull(cache.get("12"));

    for (int i = 100; i < 200; i++) {
      cache.put(String.valueOf(i), String.valueOf(i) + "_str");
    }

    assertNull(cache.get("12"));
    assertEquals("122_str", cache.get("122"));
    Thread.sleep(60);
    assertNull(cache.get("12"));
    assertNull(cache.get("122"));

  }

}
