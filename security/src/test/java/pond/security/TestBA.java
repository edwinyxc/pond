package pond.security;

import pond.security.ba.HttpBasicAuth;
import pond.web.Pond;

/**
 * Created by ed on 12/24/15.
 */
public class TestBA {

  public static void main(String[] args) {

    HttpBasicAuth ba = new HttpBasicAuth("basic")
        .validator((user, pass) -> "user".equals(user) && "pass".equals(pass)
        );

    Pond.init().debug(HttpBasicAuth.class).cleanAndBind(
        p -> p.get("/secret", ba.auth, (req, resp) -> {
          resp.send(200, "Welcome " + ba.user(req.ctx()));
        })
    ).listen(9090);

  }
}
