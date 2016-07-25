package pond.db;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pond.common.S;
import pond.common.f.Tuple;
import pond.db.connpool.ConnectionPool;
import pond.db.sql.Criterion;
import pond.db.sql.Sql;

public class JDBCTmplTest {

//  @org.junit.Test

  DB db;
//  DB mysqldb;

  @Before
  public void init() {
    S._debug_on(DB.class);

    this.db = new DB(ConnectionPool.c3p0(ConnectionPool.local("test")));
//    this.mysqldb = new DB(ConnectionPool.c3p0(ConnectionPool.mysql()));
    db.batch(
        "DROP TABLE IF EXISTS test_jdbc_t",
        "CREATE TABLE test_jdbc_t ( id varchar(64) primary key, value int(10), name double )",
        "INSERT INTO test_jdbc_t values('1',233333, 340.25)"
    );
    db.refreshStructure();
  }

  public static class test_jdbc_t extends Model {
    {
      table("test_jdbc_t");
      id("id");
      field("value");
      field("name");
    }
  }

  @Test
  public void test_updateSql() {
    db.post(t -> {
//      t.exec("use test");
      t.exec(
          Sql.update(test_jdbc_t.class)
              .set(Tuple.pair("value", null))
              .set(Tuple.pair("name", null))
              .where("id", Criterion.EQ, "1")
      );
      test_jdbc_t tt = t.recordById(test_jdbc_t.class, "1");
      S.echo(tt);
      Assert.assertNull(tt.get("value"));
      Assert.assertNull(tt.get("name"));
    });
  }

  @After
  public void after() {
    db.batch("DROP TABLE IF EXISTS test_jdbc_t");
  }


}