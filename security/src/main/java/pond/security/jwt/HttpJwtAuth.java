package pond.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.impl.crypto.MacProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.JSON;
import pond.common.S;
import pond.common.f.Callback;
import pond.common.f.Function;
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

  public HttpJwtAuth(String secretToken){
    secret = secretToken;
  }

  public Render error403(int errCode, String msg){
    return (req, resp) -> {
      resp.send(403, JSON.stringify(new HashMap(){{
        this.put("code", errCode);
        this.put("msg", msg);
      }}));
    };
  }

  public Claims getJwtClaims(WebCtx ctx) {
    return (Claims) ctx.get("jwt_claims");
  }


  public HttpJwtAuth validator( Function.F2<Boolean, String, String> user_pass_checker){
    this.user_pass_checker = user_pass_checker;
    return this;
  }

  public HttpJwtAuth onPasswordRequired( Callback.C2<Request, Response> onPasswordRequired){
    this.onPasswordRequired = onPasswordRequired;
    return this;
  }

//  public HttpJwtAuth onLoginFailed( Callback.C2<Request, Response> on_failed ){
//    this.on_failed = on_failed;
//    return this;
//  }


  public Mid auth = (req, resp) ->{

    String auth_string = req.header("Authorization");

    S._debug(logger, log -> {
      logger.debug("auth_string:" + auth_string);
    });

    if (auth_string == null ){
      resp.send(400, "auth string null");
      return;
    }

    Claims claims;

    try{
      claims = Jwts.parser()
          .setSigningKey(secret)
          .parseClaimsJws(auth_string)
          .getBody();
    }catch (SignatureException se){
      if(onPasswordRequired == null)
        resp.render(error403(403001, se.getMessage()));
      else
        onPasswordRequired.apply(req, resp);
      return;
    }
//    Date expiredAt = claims.getNotBefore();

//    if(expiredAt.getTime() > S.now()) {
//      if(onPasswordRequired == null)
//        resp.render(error403(403002, "Time has been expired"));
//      else
//        onPasswordRequired.apply(req, resp);
//      return;
//    }

    req.ctx().set("jwt_claims", claims);

  };

  public final Mid basicSignIn(String usernameLabel, String passwordLabel) {
    return (req, resp) -> {
      String username = req.paramNonBlank(usernameLabel);
      String password = req.paramNonBlank(passwordLabel);

      if(this.user_pass_checker.apply(username, password)){
        String compactJws = Jwts.builder()
            .setSubject(username)
            .signWith(SignatureAlgorithm.HS512, secret)
            .setExpiration(new Date(S.now() + age))
            .compact();
        resp.send(200, compactJws);
      }
      else
        resp.render(error403(403003, "Incorrect username or password"));
    };
  }


}
