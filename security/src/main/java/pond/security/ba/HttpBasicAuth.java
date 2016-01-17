package pond.security.ba;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.S;
import pond.common.f.Function;
import pond.web.*;

import java.util.Arrays;
import java.util.Base64;

/**
 * Created by ed on 12/24/15.
 */
public class HttpBasicAuth {
  public final static Logger logger = LoggerFactory.getLogger(HttpBasicAuth.class);

  private String in_ctx_user_id = "user_id";
  private final String realm;
  private Function.F2<Boolean, String, String> user_pass_checker;

  public HttpBasicAuth(String realm) {
    this.realm = realm;
  }

  public HttpBasicAuth validator(Function.F2<Boolean, String, String> user_pass_checker) {
    this.user_pass_checker = user_pass_checker;
    return this;
  }

  final Render requireBasicAuth = new Render() {
    @Override
    public void render(Request req, Response resp) {
      resp.header("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
      resp.send(401);
    }
  };

  public String user(WebCtx ctx){
    return (String) ctx.get(in_ctx_user_id);
  }

  public final Mid auth = (req, resp) -> {

    S._assert(user_pass_checker, "require checker for the ba");

    String auth_string = req.header("Authorization");

    S._debug(logger, log -> {
      logger.debug("auth_string:" + auth_string);
    });
    if(auth_string == null || !auth_string.startsWith("Basic ")){
      resp.render(requireBasicAuth);
      return;
    }

    String code_base64 = auth_string.substring(6);
    String decoded = new String(Base64.getDecoder().decode(code_base64));

    String[] user_and_pass = decoded.split(":");

    S._debug(logger, log -> {
      logger.debug("user_and_pass:" + Arrays.toString(user_and_pass));
    });

    if (user_and_pass.length >=2 && user_pass_checker.apply(user_and_pass[0], user_and_pass[1])) {

      S._debug(logger, log -> {
        logger.debug(String.format("Login success :%s", user_and_pass[0]));
      });
      req.ctx().set(in_ctx_user_id, user_and_pass[0]);
    } else {
      resp.render(requireBasicAuth);
    }
  };

}
