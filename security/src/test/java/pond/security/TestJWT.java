//package pond.security;
//
//import io.jsonwebtoken.impl.crypto.MacProvider;
//import pond.security.jwt.HttpJwtAuth;
//import pond.web.Pond;
//
///**
// * Created by ed on 10/14/16.
// */
//public class TestJWT {
//  public static void main(String[] args) {
//
//    HttpJwtAuth jwtAuth = new HttpJwtAuth(MacProvider.generateKey().toString());
//
//    jwtAuth.validator((user, pass) -> user.equals("1") && pass.equals("1"));
//    jwtAuth.onPasswordRequired((req, resp) -> {
//        resp.redirect("/login.html");
//    });
//
//    Pond.init().debug(HttpJwtAuth.class).cleanAndBind(
//        p -> p
//            .getEntry("/secret", jwtAuth.auth, CtxHandler.express((req, resp) -> {
//              resp.send(200, "Welcome " + jwtAuth.getJwtClaims(req.ctx()));
//            }))
//            .post("/signin", jwtAuth.basicSignIn("username", "password"))
//            .getEntry("/*", p._static("www"))
//
//    ).listen(9090);
//
//
//  }
//
//}
