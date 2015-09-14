import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pond.common.HTTP;
import pond.common.S;
import pond.web.Pond;
import pond.web.Session;
import pond.web.acl.AccessControl;
import pond.web.acl.AccessPolicy;
import pond.web.acl.SessionBasedAccessControl;
import pond.web.spi.BaseServer;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by ed on 9/11/15.
 */
public class TestSuite {
  Pond app;

  Charset utf8 = Charset.forName("UTF-8");

  @Before
  public void init() {
    app = Pond.init().debug();
    S.config.set(BaseServer.class, BaseServer.PORT, "9090");
//    System.setProperty("file.encoding","utf8");
    app.listen();
  }

  void test_auth() throws IOException {

    AccessControl ac = new SessionBasedAccessControl("user", "group");

    ac.policy(AccessPolicy.forUser((user) -> {

      S.echo("#####USER:" + user);

      if (user != null && user.equals("admin")) {
        return AccessControl.success();
      }
      return AccessControl.fail("not admin");
    }));

    //ac.policy(Policy.absolute(() -> pair(false, "now allowed")));

    app.cleanAndBind(p -> {
      p.use(Session.install);

      p.use((req, resp) -> {
        Session.get(req).set("user", "an");
      });

      p.use("/admin/.*", ac.install);

      p.get("/users/changePass", (req, resp) -> {
        resp.send(200, "OK");
      });

      p.get("/admin/changePass", (req, resp) -> {
        resp.send(200, "OK");
      });

    });

    HTTP.get("http://localhost:9090/admin/changePass", resp -> {
      Assert.assertEquals(403, resp.getStatusLine().getStatusCode());
    });

    HTTP.get("http://localhost:9090/users/changePass", resp -> {
      Assert.assertEquals(200, resp.getStatusLine().getStatusCode());
    });

  }

  @Test
  public void test() {
  }

  @After
  public void stop() {
    app.stop();
  }


}
