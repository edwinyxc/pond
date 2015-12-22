package pond.security;

import pond.core.Context;
import pond.core.Service;
import pond.core.Services;
import pond.security.rbac.RBAC;
import pond.web.InternalMids;
import pond.web.Pond;
import pond.web.Render;

/**
 * Created by ed on 12/20/15.
 */
public class TestApp {

  public static void main(String[] args) {

    Services.add("kill_pig", new Service(ctx -> {
      ctx.push("PIG HAS BEEN KILLLLED");
    }));

    Services.add("kill_dog", new Service(ctx -> {
      ctx.push("dog HAS BEEN KILLLLED");
    }));

    RBAC rbac = new RBAC("killer", context -> (String) context.get("user_id"));

    rbac.reset();

    rbac.user_add("admin", "admin");
    rbac.user_add("normal", "n");

    rbac.role_add("dog_killer", "kill dog");
    rbac.role_add_serv("dog_killer", "kill_dog");

    rbac.role_add("pig_killer", "kill pig");
    rbac.role_add_serv("pig_killer", "kill_pig");

    rbac.user_add_role("admin", "dog_killer");
    rbac.user_add_role("admin", "pig_killer");

    rbac.user_add_role("normal", "dog_killer");


    Pond.init().debug().cleanAndBind(p -> {
      p.use("/config", rbac.controller);
      p.get("/exec/:user_id",
            (req, resp) -> {
              resp.render(Render.dump(
                  new Context("secure")
                      .interceptor(rbac.interceptor())
                      .set("user_id", req.param("user_id"))
                      .exec(
                      Services.get("kill_dog"),
                      Services.get("kill_pig"),
                      Services.get("kill_pig"),
                      Services.get("kill_pig"),
                      Services.get("kill_pig"),
                      Services.get("kill_pig"),
                      Services.get("kill_pig"),
                      Services.get("kill_pig"),
                      Services.get("kill_pig"),
                      Services.get("kill_pig"),
                      Services.get("kill_dog"),
                      Services.get("kill_dog"),
                      Services.get("kill_dog"),
                      Services.get("kill_dog"),
                      Services.get("kill_dog"),
                      Services.get("kill_pig"),
                      Services.get("kill_pig"),
                      Services.get("kill_pig"),
                      Services.get("kill_pig"),
                      Services.get("kill_pig"),
                      Services.get("kill_pig"),
                      Services.get("kill_pig"),
                      Services.get("kill_pig"),
                      Services.get("kill_pig")
                  )));
            });
      p.otherwise(InternalMids.FORCE_CLOSE);
    }).listen(8080);
  }

}
