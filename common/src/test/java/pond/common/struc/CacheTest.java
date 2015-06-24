package pond.common.struc;

import org.junit.Test;
import pond.common.S;

public class CacheTest {
    @Test
    public void test() {
        Cache<String, String> cache = Cache.<String, String>lruCache(5)
                .onNotFound(k -> "not found: " + k);
        cache.get("1", "1332");
        cache.get("2", "68");
        cache.get("3", "123321");
        cache.get("4", "2333");
        cache.get("5", "33");
        cache.get("6", "333");
        cache.get("7", "333");
        String tmp;
        tmp = cache.get("3");
        tmp = cache.get("3");
        tmp = cache.get("3");
        tmp = cache.get("3");
        S.echo(tmp);
        S.echo(cache.asMap());
        cache.put("1", "new1");
        cache.put("2", "new2");
        S.echo(cache.get("2"));
        cache.put("4", "new4");
        cache.put("5", "new5");
        S.echo(cache.get("3"));
        cache.put("6", "new5");
        S.echo(cache.get("2"));
        cache.put("7","new5");
        cache.put("8", "new5");
        S.echo(cache.get("0", "new0"));
        S.echo(cache.get("1",(cs)->{cs.put("1","new1");return "new1";}));
        S.echo(cache.asMap());
    }
}
