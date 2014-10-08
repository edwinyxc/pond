package pond.core;

import org.junit.*;
import static org.junit.Assert.*;
import pond.common.S;
import pond.common.SPILoader;
import pond.core.spi.JsonService;

import java.util.Map;

/**
 * Created by ed on 8/11/14.
 */
public class JsonTest {

    
    @Test
    public void json_fromstring() {
        String json = "{a:'A',b:'B',c:'C'}";
        Map map =
        Pond.json().fromString(Map.class, json);
        assertEquals(map.get("a"),"A");
        assertEquals(map.get("b"),"B");
        assertEquals(map.get("c"),"C");
    }

}
