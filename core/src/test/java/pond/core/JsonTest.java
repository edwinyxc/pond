package pond.core;

import pond.common.S;
import pond.common.SPILoader;
import pond.core.spi.JsonService;

import java.util.Map;

/**
 * Created by ed on 8/11/14.
 */
public class JsonTest {

    public static void main(String[] args){
        JsonService s = SPILoader.service(JsonService.class);
        String json = "{a:'A',b:'B',c:'C'}";
        Map map = s.fromString(Map.class,json);
        S.echo(map);
    }
}
