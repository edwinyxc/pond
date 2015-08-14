package pond.core;

import org.junit.Test;
import pond.common.JSON;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by ed on 8/11/14.
 */
public class JsonTest {


  @Test
  public void json_fromstring() {
    String json = "{a:'A',b:'B',c:'C'}";
    Map map = JSON.parse(json);
    assertEquals(map.get("a"), "A");
    assertEquals(map.get("b"), "B");
    assertEquals(map.get("c"), "C");
  }

}
