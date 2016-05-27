package pond.web;

import io.netty.util.CharsetUtil;
import org.junit.Before;
import org.junit.Test;
import pond.common.HTTP;
import pond.common.JSON;
import pond.common.S;
import pond.common.STREAM;
import pond.db.DB;
import pond.db.JDBCTmpl;
import pond.db.connpool.ConnectionPool;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RestfulRouterTest {

  DB db;
  Pond app;

  @Before
  public void init() {
    S._debug_on(DB.class, JDBCTmpl.class);

    this.db = new DB(ConnectionPool.c3p0(ConnectionPool.local("test")));

    db.batch(
        "DROP TABLE IF EXISTS test_pagination",
        "CREATE TABLE test_pagination ( " +
            "id varchar(64) primary key auto_increment," +
            " name varchar(2000), " + //string eq lk
            " birthday bigint(11), " + // between
            " type varchar(30), " + //in
            " )",
        "INSERT INTO test_pagination (name,birthday,type)values('yxc',123456,'admin')",
        "INSERT INTO test_pagination (name,birthday,type)values('yxc',123456,'admin')",
        "INSERT INTO test_pagination (name,birthday,type)values('yxc',123456,'admin')",
        "INSERT INTO test_pagination (name,birthday,type)values('yxc',123456,'admin')",
        "INSERT INTO test_pagination (name,birthday,type)values('yxc',123456,'admin')",
        "INSERT INTO test_pagination (name,birthday,type)values('yxc',123456,'admin')",
        "INSERT INTO test_pagination (name,birthday,type)values('yxc',123456,'admin')",
        "INSERT INTO test_pagination (name,birthday,type)values('yxc',123456,'admin')",
        "INSERT INTO test_pagination (name,birthday,type)values('yxc',123456,'admin')",
        "INSERT INTO test_pagination (name,birthday,type)values('yxc',123456,'admin')",
        "INSERT INTO test_pagination (name,birthday,type)values('yxc',123456,'admin')",
        "INSERT INTO test_pagination (name,birthday,type)values('sky',123457,'debugger')",
        "INSERT INTO test_pagination (name,birthday,type)values('sky',123457,'debugger')",
        "INSERT INTO test_pagination (name,birthday,type)values('sky',123457,'debugger')",
        "INSERT INTO test_pagination (name,birthday,type)values('sky',123457,'debugger')",
        "INSERT INTO test_pagination (name,birthday,type)values('sky',123457,'debugger')",
        "INSERT INTO test_pagination (name,birthday,type)values('sky',123457,'debugger')",
        "INSERT INTO test_pagination (name,birthday,type)values('sky',123457,'debugger')",
        "INSERT INTO test_pagination (name,birthday,type)values('sky',123457,'debugger')",
        "INSERT INTO test_pagination (name,birthday,type)values('sky',123457,'debugger')",
        "INSERT INTO test_pagination (name,birthday,type)values('sky',123457,'debugger')",
        "INSERT INTO test_pagination (name,birthday,type)values('sky',123457,'debugger')",
        "INSERT INTO test_pagination (name,birthday,type)values('skyyy',123457,'debugger')",
        "INSERT INTO test_pagination (name,birthday,type)values('skyyy',123457,'debugger')",
        "INSERT INTO test_pagination (name,birthday,type)values('skyyy',123457,'debugger')",
        "INSERT INTO test_pagination (name,birthday,type)values('skyyy',123457,'debugger')",
        "INSERT INTO test_pagination (name,birthday,type)values('skyyy',123457,'debugger')",
        "INSERT INTO test_pagination (name,birthday,type)values('skyyy',123457,'debugger')",
        "INSERT INTO test_pagination (name,birthday,type)values('skyyy',123457,'debugger')",
        "INSERT INTO test_pagination (name,birthday,type)values('skyyy',123457,'debugger')",
        "INSERT INTO test_pagination (name,birthday,type)values('skyyy',123457,'debugger')",
        "INSERT INTO test_pagination (name,birthday,type)values('skyyy',123457,'debugger')",
        "INSERT INTO test_pagination (name,birthday,type)values('skyyy',123457,'debugger')",
        "INSERT INTO test_pagination (name,birthday,type)values('yxccc',123200,'super_admin')",
        "INSERT INTO test_pagination (name,birthday,type)values('yxccc',123200,'super_admin')",
        "INSERT INTO test_pagination (name,birthday,type)values('yxccc',123200,'super_admin')",
        "INSERT INTO test_pagination (name,birthday,type)values('yxccc',123200,'super_admin')",
        "INSERT INTO test_pagination (name,birthday,type)values('yxccc',123200,'super_admin')",
        "INSERT INTO test_pagination (name,birthday,type)values('yxccc',123200,'super_admin')",
        "INSERT INTO test_pagination (name,birthday,type)values('yxccc',123200,'super_admin')",
        "INSERT INTO test_pagination (name,birthday,type)values('yxccc',123200,'super_admin')",
        "INSERT INTO test_pagination (name,birthday,type)values('yxccc',123200,'super_admin')",
        "INSERT INTO test_pagination (name,birthday,type)values('yxccc',123200,'super_admin')",
        "INSERT INTO test_pagination (name,birthday,type)values('yxccc',123200,'super_admin')",
        "INSERT INTO test_pagination (name,birthday,type)values('ed',222222,'foo')",
        "INSERT INTO test_pagination (name,birthday,type)values('ed',222222,'foo')",
        "INSERT INTO test_pagination (name,birthday,type)values('ed',222222,'foo')",
        "INSERT INTO test_pagination (name,birthday,type)values('ed',222222,'foo')",
        "INSERT INTO test_pagination (name,birthday,type)values('ed',222222,'foo')",
        "INSERT INTO test_pagination (name,birthday,type)values('ed',222222,'foo')",
        "INSERT INTO test_pagination (name,birthday,type)values('ed',222222,'foo')",
        "INSERT INTO test_pagination (name,birthday,type)values('ed',222222,'foo')",
        "INSERT INTO test_pagination (name,birthday,type)values('ed',222222,'foo')",
        "INSERT INTO test_pagination (name,birthday,type)values('ed',222222,'foo')",
        "INSERT INTO test_pagination (name,birthday,type)values('ed',222222,'foo')"
    );

    app = Pond.init(p -> {
      p.use("/*", new RestfulRouter<TestModel>(TestModel.class, db){{

        get("/can_i_come_in",(req, resp) -> {
          resp.send(200,"OK");
        });

        REST.all();
      }});
    }).listen(9091);

  }

  @Test
  public void test_conflict() throws IOException{
    HTTP.get("http://localhost:9091/can_i_come_in", resp -> {
      String s = null;
      try {
        s = STREAM.readFully(S._try_ret(() -> resp.getEntity().getContent()), CharsetUtil.UTF_8);
        assertEquals("OK", s);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  @Test
  public void test_search_single_item_by_id() throws IOException {
    HTTP.get("http://localhost:9091/1", resp -> {
      String s = null;
      try {
        s = STREAM.readFully(S._try_ret(() -> resp.getEntity().getContent()), CharsetUtil.UTF_8);
        List<Map> arr = JSON.parseArray(s);
        assertEquals(arr.size(), 1);
        assertEquals(arr.get(0).get("name"), "yxc");
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  @Test
  public void test_unified_search() throws IOException {
    HTTP.get("http://localhost:9091/?name=yxc", resp -> {
      String s = null;
      try {
        s = STREAM.readFully(S._try_ret(() -> resp.getEntity().getContent()), CharsetUtil.UTF_8);
        List<Map> arr = JSON.parseArray(s);
        assertEquals(11,arr.size());
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
    HTTP.get("http://localhost:9091/?birthday=btwn,123199,123456", resp -> {
      String s = null;
      try {
        s = STREAM.readFully(S._try_ret(() -> resp.getEntity().getContent()), CharsetUtil.UTF_8);
        List<Map> arr = JSON.parseArray(s);
        assertEquals(22,arr.size());
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }
}