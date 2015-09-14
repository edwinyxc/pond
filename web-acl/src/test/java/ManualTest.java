import pond.common.S;
import pond.web.Pond;
import pond.web.Session;
import pond.web.acl.AccessControl;
import pond.web.acl.AccessPolicy;
import pond.web.acl.SessionBasedAccessControl;

/**
 * Created by ed on 9/11/15.
 */
public class ManualTest {

  public static void test() {

    S._debug_on(AccessControl.class);

    Pond app = Pond.init().debug().listen(9090);

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
      p.use(Session.install());

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
  }

  public static void main(String args[]) {
    test();
  }
}
