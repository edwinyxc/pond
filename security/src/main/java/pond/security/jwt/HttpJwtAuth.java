package pond.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.JSON;
import pond.common.S;
import pond.common.f.Callback;
import pond.common.f.Function;
import pond.common.f.Tuple;
import pond.web.*;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by ed on 10/13/16.
 * Using in-header-JWT to authenticate user
 */
public class HttpJwtAuth {

    //TODO to be polished
    public final static Logger logger = LoggerFactory.getLogger(HttpJwtAuth.class);

    private String secret = MacProvider.generateKey().toString();

    private Function.F2<Boolean, String, String> user_pass_checker;

    private Callback.C2<Request, Response> onPasswordRequired;

    private long age = 30 * 60 * 10000; /* 30min */

    public HttpJwtAuth(String secretToken) {
        secret = secretToken;
    }

    public Claims getJwtClaims(HttpCtx ctx) {
        return (Claims) ctx.get("jwt_claims");
    }

    public String user(HttpCtx ctx) {
        return getJwtClaims(ctx).getSubject();
    }


    public HttpJwtAuth validator(Function.F2<Boolean, String, String> user_pass_checker) {
        this.user_pass_checker = user_pass_checker;
        return this;
    }

    public HttpJwtAuth onPasswordRequired(Callback.C2<Request, Response> onPasswordRequired) {
        this.onPasswordRequired = onPasswordRequired;
        return this;
    }

//  public HttpJwtAuth onLoginFailed( Callback.C2<Request, Response> on_failed ){
//    this.on_failed = on_failed;
//    return this;
//  }

    public WellDefinedHandler auth = CtxHandler.def(
            ParamDef.header("Authorization"),
            ResultDef.error(400, "Authorization null"),
            ResultDef.errorJSON(403, "Detailed Error"),

            (ctx, auth, require_auth, forbidden) -> {
                Request req = ((HttpCtx) ctx).req;
                Response resp = ((HttpCtx) ctx).resp;

                String auth_string = req.header("Authorization");
                S._debug(logger, log -> {
                    logger.debug("auth_string:" + auth_string);
                });

                if (auth_string == null) {
                    if (onPasswordRequired == null)
                        ctx.result(require_auth, "auth string null");
                    else
                        onPasswordRequired.apply(req, resp);
                    return;
                }

                Claims claims;

                try {
                    claims = Jwts.parser()
                            .setSigningKey(secret)
                            .parseClaimsJws(auth_string)
                            .getBody();
                } catch (Exception e) {
                    if (onPasswordRequired == null) {
                        S._debug(logger, log -> {
                            logger.debug("Exception while parsing:", e);
                        });
                        ctx.result(forbidden, Tuple.pair(403001, e.getMessage()));
                    } else {
                        onPasswordRequired.apply(req, resp);
                    }
                    return;
                }

                ctx.set("jwt_claims", claims);

            });

    public final WellDefinedHandler basicSignIn(String usernameLabel, String passwordLabel) {
        return CtxHandler.def(
                ParamDef.str(usernameLabel).required("username must not null"),
                ParamDef.str(passwordLabel).required("password must not null"),


                ResultDef.text("compactJWS"),
                ResultDef.errorJSON(403, "JSON formatted error detail info with inner code and msg"),

                (ctx, username, password, ok, forbidden) -> {

                    if (this.user_pass_checker.apply(username, password)) {
                        String compactJws = Jwts.builder()
                                .setSubject(username)
                                .signWith(SignatureAlgorithm.HS512, secret)
                                .setExpiration(new Date(S.now() + age))
                                .compact();
                        ctx.result(ok, compactJws);
                    } else
                        ctx.result(forbidden, Tuple.pair(403003, "Incorrect username or password"));
                });
    }

}
