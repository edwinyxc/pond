package pond.db;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pond.common.S;
import pond.db.connpool.ConnectionPool;

import java.util.List;

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
        "CREATE TABLE test ( id varchar(64) primary key, value varchar(2000) )",
        "INSERT INTO test values('2333','233333')",
        "INSERT INTO test values('2334','2333334')"
    );
  }

  @Test
  public void testSql() {

    List<Record> rlist = this.db.get(t -> t.query("select value from test where id = '2333'"));

    String v = S._tap_nullable(_for(rlist).first(), first -> first.get("value"));
    S.echo(S.dump(rlist));
    Assert.assertEquals("233333", v);

  }

  @After
  public void after() {
    db.batch("DROP TABLE IF EXISTS test");
  }
}
