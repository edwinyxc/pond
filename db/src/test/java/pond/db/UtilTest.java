package pond.db;

import org.junit.Test;
import pond.common.S;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class UtilTest {

  @Test
  public void testA() {
    String a = String.format("jdbc:mysql://%s/%s?%s", "ssss", "weee", "wwww");
    assertEquals("jdbc:mysql://ssss/weee?wwww", a);
  }

  @Test
  public void record_map_test() {

    Model a = new Model(){{
      id("id");
      field("f1");
    }};

    a.set("id", "123").set("f1","15");


    Model b = new Model(){{
      id("id");
      field("f1");
    }};

    b.set("id","333").set("f1","16");

    a.mergeExceptId(b.toMap());
    assertEquals(a.id(), "123");
  }

  @Test
  public void test() {
    Object a = Collections.emptyMap().get("ss");
    System.out.print(a);
    S.echo(5000 * 3 + 8000 * 2 + 6000 + 12000);
    S.echo(5000 * 2 + 8000 * 4 + 12000);
  }
}
