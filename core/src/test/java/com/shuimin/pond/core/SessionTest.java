package com.shuimin.pond.core;

import com.shuimin.common.S;
import com.shuimin.pond.core.mw.session.Session;
import com.shuimin.pond.core.mw.session.SessionInstaller;


public class SessionTest {

    public static void main(String[] args) {
        Pond app = Pond.init().debug();
        app.before(new SessionInstaller());
        app.get("/ses", (req, res) -> {
                    Session ses = Session.get();
                    ses.set("i", (Integer) S._notNullElse(ses.get("i"), 0) + 1);

                    res.write(S.dump(req.ctx()));
                    res.write("<br>");
                    res.write(ses.hashCode() + " " + S.dump(ses));
                    res.send(200);
                }
        );
        app.get("/read", (req, resp) -> {
            resp.send("<p>i=" + Session.get().get("i") + "</p>");
        });
        app.listen(8080);
    }

}