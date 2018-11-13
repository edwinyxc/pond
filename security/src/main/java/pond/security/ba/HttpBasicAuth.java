//package pond.security.ba;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import pond.common.S;
//import pond.common.f.Function;
//import pond.web.*;
//import pond.web.restful.API;
//import pond.web.restful.APIHandler;
//import pond.web.HttpCtx;
//import pond.web.restful.ParamDef;
//import pond.web.restful.ResultDef;
//
//import java.util.Arrays;
//import java.util.Base64;
//
///**
// * Created by ed on 12/24/15.
// */
//public class HttpBasicAuth {
//    public final static Logger logger = LoggerFactory.getLogger(HttpBasicAuth.class);
//
//    private static String IN_CTX_USER_ID = "user_id";
//    private Function.F2<Boolean, String, String> user_pass_checker;
//    private String realm;
//
//    public HttpBasicAuth(String realm) {
//        this.realm = realm;
//    }
//
//    public HttpBasicAuth validator(Function.F2<Boolean, String, String> user_pass_checker) {
//        this.user_pass_checker = user_pass_checker;
//        return this;
//    }
//
//
//    public String user(HttpCtx ctx) {
//        return (String) ctx.get(IN_CTX_USER_ID);
//    }
//
//    public final APIHandler auth = API.def(
//            ParamDef.header("Authorization"),
//
//            ResultDef.<Void>any(401, "require Basic Authenticate header", (ctx, t) -> {
//                Response resp = ((HttpCtx) ctx).resp;
//                resp.header("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
//                resp.send(401);
//            }),
//
//            (ctx, auth_string, require_header) ->
//            {
//                S._assert(user_pass_checker, "require checker for the ba");
//                S._debug(logger, log -> {
//                    logger.debug("auth_string:" + auth_string);
//                });
//
//                if (auth_string == null || !auth_string.startsWith("Basic ")) {
//                    ctx.result(require_header);
//                    return;
//                }
//
//                String code_base64 = auth_string.substring(6);
//                String decoded = new String(Base64.getDecoder().decode(code_base64));
//
//                String[] user_and_pass = decoded.split(":");
//
//                S._debug(logger, log -> {
//                    logger.debug("user_and_pass:" + Arrays.toString(user_and_pass));
//                });
//
//                if (user_and_pass.length >= 2 && user_pass_checker.apply(user_and_pass[0], user_and_pass[1])) {
//                    S._debug(logger, log -> {
//                        logger.debug(String.format("Login success :%s", user_and_pass[0]));
//                    });
//                    ctx.set(IN_CTX_USER_ID, user_and_pass[0]);
//                } else {
//                    ctx.result(require_header);
//                }
//            }
//    );
//
//}
