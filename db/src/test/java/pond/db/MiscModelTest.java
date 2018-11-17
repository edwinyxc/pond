package pond.db;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pond.common.S;
import pond.db.connpool.ConnectionPool;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static pond.common.S._for;

/**
 * Created by ed on 10/31/14.
 */
public class MiscModelTest {

  DB db;

  @Before
  public void init() {
    S._debug_on(DB.class);

    this.db = new DB(ConnectionPool.c3p0(ConnectionPool.local("test")));

    db.batch(
        "DROP TABLE IF EXISTS test",
        "CREATE TABLE test ( id varchar(64) primary key, value int(10) )",
        "INSERT INTO test values('2333','233333')",
        "INSERT INTO test values('2334','2333334')",
        "INSERT INTO test (id) values ('2337')"
    );
  }


  @Test
  public void test_if_a_null_int_is_transformed_into_0 (){
    List<Record> rlist =  this.db.get(t -> t.query("select value from test where id = '2337'"));
    Record r = rlist.get(0);
    Integer value = r.get("value");
    S.echo("VALUE", value);
    assertNull(value);
  }

  @Test
  public void testSql() {

    List<Record> rlist = this.db.get(t -> t.query("select value from test where id = '2333'"));

    int v = S._tap_nullable(_for(rlist).first(), first -> first.get("value"));

    S.echo(S.dump(rlist));

    assertEquals(233333, v);

  }


  @Test
  public void testExist() {
    assertEquals(Boolean.TRUE, db.get(t -> t.recordExists(TestModel.class, "2333")));
    assertEquals(Boolean.FALSE, db.get(t -> t.recordExists(TestModel.class, "111")));
    assertEquals(Boolean.FALSE, db.get(t -> t.recordExists(TestModel.class, "")));
  }



  @After
  public void after() {
    db.batch("DROP TABLE IF EXISTS test");
  }
}
