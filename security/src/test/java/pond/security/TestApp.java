package pond.security;

import pond.common.S;
import pond.core.ExecutionContext;
import pond.core.Executor;
import pond.core.Service;
import pond.core.Services;
import pond.security.rbac.RBAC;
import pond.web.CtxExec;
import pond.web.InternalMids;
import pond.web.Pond;
import pond.web.Render;

/**
 * Created by ed on 12/20/15.
 */
public class TestApp {




  public static void main(String[] args){

    Services.register("kill_pig", new Service(ctx -> {
      ctx.push("PIG HAS BEEN KILLLLED");
    }));

    Services.register("kill_dog", new Service(ctx -> {
      ctx.push("dog HAS BEEN KILLLLED");
    }));

    RBAC rbac = new RBAC("killer", context -> (String)context.get("user_id"));

    rbac.reset();

    rbac.user_add("admin", "admin");
    rbac.user_add("normal", "n");

    rbac.role_add("dog_killer","kill dog");
    rbac.role_add_serv("dog_killer", "kill_dog");

    rbac.role_add("pig_killer", "kill pig");
    rbac.role_add_serv("pig_killer", "kill_pig");

    rbac.user_add_role("admin", "dog_killer");
    rbac.user_add_role("admin", "pig_killer");

    rbac.user_add_role("normal", "dog_killer");

    Executor executor = new Executor().interceptor(rbac.interceptor());

    Pond.init().debug().cleanAndBind(p -> {
      p.use("/config", rbac.controller);
      p.get("/exec/:user_id", (req, resp) -> {
        req.ctx().set("user_id", req.param("user_id"));
        executor.exec(
            req.ctx(),
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
        );
        resp.render(Render.dump(req.ctx()));
      });

      p.otherwise(InternalMids.FORCE_CLOSE);

    }).listen(8080);
    //dynamic execution procedure


  }

}
