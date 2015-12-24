package pond.security;

import pond.security.rbac.RBAC;
import pond.web.InternalMids;
import pond.web.Pond;
import pond.web.Render;

/**
 * Created by ed on 12/20/15.
 */
public class TestApp {

  public static void main(String[] args) {

    RBAC rbac = new RBAC("killer");

    rbac.reset();

    rbac.user_add("admin", "admin");
    rbac.user_add("normal", "n");

    rbac.role_add("dog_killer", "kill dog");
    rbac.role_add("pig_killer", "kill pig");

    rbac.user_add_role("admin", "dog_killer");
    rbac.user_add_role("admin", "pig_killer");
    rbac.user_add_role("normal", "dog_killer");


    Pond.init().debug().cleanAndBind(p -> {

      p.use("/config/*", rbac.controller);
      p.get("/exec/:user_id", (req, resp) -> {

        RBAC.Roles roles = rbac.forUser(req.param("user_id"));
        if (roles.hasNone("dog_killer", "pig_killer")) {
          resp.send(403);
          return;
        }
        String some = "";

        if(roles.hasEvery("dog_killer")){
          some += "kill a dog;";
        }

        if(roles.hasEvery("pig_killer")){
          some += "kill a killer";
        }

        resp.send(200,some);

      });

      p.get("/roles/:user_id", (req, resp) -> {
        resp.render(Render.dump(rbac.forUser(req.param("user_id"))));
      });

      p.otherwise(InternalMids.FORCE_CLOSE);

    }).listen(8080);
  }

}
