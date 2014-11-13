package pond.core;

import pond.common.S;
import pond.core.session.SessionManager;

import static pond.common.S.file.loadProperties;

public class SessionTest {


    public static void main(String[] args) {

        Pond app = Pond.init( p -> {
            p.loadConfig(loadProperties("pond.conf"));
            p.config.put(SessionManager.SESSION_LIFETIME, "5");
        }).debug();

        app.before(app.useSession());
        app.get("/ses", (req, res) -> {
                    Session ses = req.session();
                    ses.set("i", (Integer) S._notNullElse(ses.get("i"), 0) + 1);

                    res.write(S.dump(req.ctx()));
                    res.write("<br>");
                    res.write(ses.hashCode() + " " + S.dump(ses));
                    res.send(200);
                }
        );
        app.get("/read", (req, resp) -> {
            resp.send("<p>i=" + req.session().get("i") + "</p>");
        });
        app.listen(8080);
    }

}