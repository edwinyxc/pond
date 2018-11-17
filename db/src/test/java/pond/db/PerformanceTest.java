package pond.db;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import pond.common.S;
import pond.common.f.Function;
import pond.common.f.Tuple;
import pond.db.connpool.ConnectionPool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by ed on 1/28/15.
 */
public class PerformanceTest {

  ConnectionPool cp;
  DB db;
  long time;

  @Before
  public void before(){
    time = S.now();
    //cp = ConnectionPool.simplePool(ConnectionPool.local("test"));
    cp = ConnectionPool.c3p0(ConnectionPool.local("test"));
    db = new DB(cp);
    db.batch("DROP TABLE IF EXISTS p_test",
             "CREATE TABLE p_test (id varchar(60), percent varchar(60), title varchar(60))");

    for(int i = 0; i <200; i++) {
      String final_idx = "" + i;
      db.post(t -> t.exec("INSERT INTO p_test VALUES(?,?,?)", final_idx, Math.random()*100, Math.random()*10 ));
    }
    S.echo("creation 10000 usage:",S.now() - time ,"ms");
  }

  @Ignore
  @Test
  public void test_with_pond_db(){

    Function<Map, ResultSet> mapper = (rs -> new HashMap() {{
      try {
        this.put("title", rs.getString("title"));
        this.put("id", rs.getString("id"));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }});

    time = S.now();

    for (int i = 0; i < 30; i++) {
      S.echo(S.time(() -> db.get(t -> t.query(mapper, "SELECT * FROM p_test GROUP BY title"))));
      S.echo(S.time(() -> db.get(t -> t.query("SELECT * FROM p_test GROUP BY title"))));
    }

    S.echo("[POND-DB]group 30 usage:",S.now() - time ,"ms");
  }




  @After
  public void after(){
    db.batch("DROP TABLE IF EXISTS p_test");
  }

  @Ignore
  @Test
  public void test_with_native_jdbc() {
    try {
      long start = S.now();
      Connection conn = cp.getConnection();
      long time_conn = S.now();
      ResultSet rs = conn.
          prepareStatement("SELECT title, size(*)  percent FROM p_test GROUP BY title")
          .executeQuery();
      List r = new ArrayList<>();
      while (rs.next()) {
        Tuple<String, Integer> result = Tuple.pair(rs.getString("title"), rs.getInt("percent"));
        r.add(result);
      }
      long time_data_fetch = S.now();
      conn.close();
      S.echo(r);
      long time_conn_close = S.now();


      S.echo("conn_creation:" + String.valueOf(time_conn - start));
      S.echo("data_fetch:" + String.valueOf(time_data_fetch - time_conn));
      S.echo("close:" + String.valueOf(time_conn_close - time_data_fetch));

    } catch (SQLException e) {
      e.printStackTrace();
    }

  }
}
