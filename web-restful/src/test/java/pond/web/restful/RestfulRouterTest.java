package pond.web.restful;

import io.netty.util.CharsetUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pond.common.JSON;
import pond.common.S;
import pond.common.STREAM;
import pond.db.DB;
import pond.db.JDBCTmpl;
import pond.db.connpool.ConnectionPool;
import pond.web.Pond;
import pond.web.RestfulRoutes;
import pond.web.Router;

import java.io.IOException;
import java.util.HashMap;
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
      p.use("/*", new Router() {{

        get("/can_i_come_in", (req, resp) -> {
          resp.send(200, "OK");
        });

        new RestfulRoutes<>(this, db, TestModel.class)
            .id()
            .index()
            .postRoot()
            .putRoot()
            .delRoot();
      }});
    }).listen(9091);

  }


  public void test_conflict() throws IOException {
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

  public void test_search_single_item_by_id() throws IOException {
    HTTP.get("http://localhost:9091/1", resp -> {
      String s = null;
      try {
        s = STREAM.readFully(S._try_ret(() -> resp.getEntity().getContent()), CharsetUtil.UTF_8);
        List<Map> arr = JSON.parseArray(s);
        S.echo("ARRR", arr);
        assertEquals(arr.size(), 1);
        assertEquals(arr.get(0).get("name"), "yxc");
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  public void test_insert() throws IOException {
    HTTP.post("http://localhost:9091/",new HashMap<String, Object>(){{
      put("id", 121);
      put("name", "name1");
      put("birthday", 111);
      put("type", "type3");
    }},resp -> {
      String s = null;
      try {
        s = STREAM.readFully(S._try_ret(() -> resp.getEntity().getContent()), CharsetUtil.UTF_8);
        S.echo("###", s);
        Map obj = JSON.parse(s);
        assertEquals(obj.get("name"), "name1");
        assertEquals(obj.get("birthday"), "111");
        assertEquals(obj.get("type"), "type3");
        S.echo("POST", obj);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  public void test_update() throws IOException {
    HTTP.put("http://localhost:9091/1", new HashMap<String, Object>() {{
      put("name", "name1");
      put("birthday", 111);
      put("type", "type3");
    }}, resp -> {
      String s = null;
      try {
        s = STREAM.readFully(S._try_ret(() -> resp.getEntity().getContent()), CharsetUtil.UTF_8);
      } catch (IOException e) {
        e.printStackTrace();
      }
      Map<String,Object> ret = JSON.parse(s);
      S.echo("PUT", ret);
      assertEquals(111, Integer.parseInt((String)ret.get("birthday")));
    });
  }

  public void test_delete() throws IOException {
    HTTP.delete("http://localhost:9091/1", resp -> {
      assertEquals(204, resp.getStatusLine().getStatusCode());
    });
  }

  public void test_unified_search() throws IOException {
    HTTP.get("http://localhost:9091/?name=yxc", resp -> {
      String s = null;
      try {
        s = STREAM.readFully(S._try_ret(() -> resp.getEntity().getContent()), CharsetUtil.UTF_8);
        List<Map> arr = JSON.parseArray(s);
        S.echo("ARR", arr);
        S.echo("headers", resp.getAllHeaders());
        assertEquals(11, arr.size());
      } catch (IOException e) {
        e.printStackTrace();
      }
    });

    HTTP.get("http://localhost:9091/?birthday=btwn,123199,123456", resp -> {
      String s = null;
      try {
        s = STREAM.readFully(S._try_ret(() -> resp.getEntity().getContent()), CharsetUtil.UTF_8);
        List<Map> arr = JSON.parseArray(s);
        assertEquals(22, arr.size());
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  @Test
  public void testAll() throws IOException {
    test_conflict();
    test_insert();
    test_search_single_item_by_id();
    test_unified_search();
    test_update();
    test_delete();
  }

  @After
  public void stop() {
    app.stop();
  }
}