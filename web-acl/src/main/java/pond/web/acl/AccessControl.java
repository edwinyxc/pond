package pond.web.acl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.f.Function;
import pond.common.f.Tuple;
import pond.web.Ctx;
import pond.web.Mid;
import pond.web.Route;
import pond.web.http.HttpMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ed on 9/11/15.
 */
public class AccessControl {

  final static Logger logger = LoggerFactory.getLogger(AccessControl.class);

  protected final String INCTX_USER_TOKEN_LABEL = "CTX_UT";

  protected final String INCTX_GROUP_TOKEN_LABEL = "CTX_GT";

  private Function<String, Ctx> user_token_provider = ctx -> (String) ctx.get(INCTX_USER_TOKEN_LABEL);

  private Function<String, Ctx> group_token_provider = ctx -> (String) ctx.get(INCTX_GROUP_TOKEN_LABEL);


  private List<Function.F5<Tuple<Boolean, String>, String, String, Route, HttpMethod, Ctx>>

      acl = new ArrayList<>();

  /**
   * <p>
   * AccessControl is access filter framework
   * </p>
   */
  public AccessControl() { }

  public AccessControl userTokenGetter(Function<String, Ctx> provider) {
    this.user_token_provider = provider;
    return this;
  }

  public AccessControl groupTokenGetter(Function<String, Ctx> provider) {
    this.group_token_provider = provider;
    return this;
  }

  public AccessControl policy(AccessPolicy... policies) {
    acl.addAll(Arrays.asList(policies));
    return this;
  }

  public static Tuple<Boolean, String> fail(String txt) {
    return Tuple.t2(false, txt);
  }

  public static Tuple<Boolean, String> fail() {
    return fail("fail");
  }

  public static Tuple<Boolean, String> success(String txt) {
    return Tuple.t2(true, txt);
  }

  public static Tuple<Boolean, String> success() {
    return success("success");
  }

  public Mid install = (req, resp) -> {

    Ctx ctx = req.ctx();

    Route route = ctx.route();

    HttpMethod method = ctx.method();

////    Session session = Session.get(req);
//
//    if (session == null) {
//      throw new NullPointerException("session");
//    }

    String user_token, group_token = null;

    user_token = user_token_provider.apply(ctx);

    group_token = group_token_provider.apply(ctx);

    for (Function.F5<Tuple<Boolean, String>, String, String, Route, HttpMethod, Ctx>
        a : acl) {

      Tuple<Boolean, String> result = a.apply(user_token, group_token, route, method, ctx);
      if (!result._a) {
        String err_msg = result._b;
        resp.sendError(403, err_msg);
        return;
      }

    }

    //pass
  };

}
