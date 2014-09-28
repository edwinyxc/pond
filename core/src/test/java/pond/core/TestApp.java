package pond.core;

import pond.common.S;
import pond.common.f.Holder;
import pond.core.router.Router;

import java.util.Map;

/**
 * Created by ed on 7/9/14.
 */
public class TestApp {

    public static void basic() {
        Pond app = Pond.init();
        app.get("/",(req, res ) -> {
            req.param("user", "hellosdddaweqweqw" +
                    "asdasdasdasd" +
                    "asdqweqwe1231231321");
        });
        app.get("/",(req, res ) -> {
            String user = req.param("user");
            System.out.println(user);
            res.send("<p>" + user + "</p>");
        });
        app.listen(8080);
    }

    public static void router() {
        Pond app =
                Pond.init().debug();
        Router router = new Router();
        router.get("/add", (req, resp) -> resp.send("add"))
                .get("/del", (req, resp) -> resp.send("del"));

        app.get("/", (req, resp) -> {
            resp.send("root");
        }).get("/${id}",
                (req, resp) -> resp.send(req.param("id"))
        ).get("/${id}/text", (req, resp) -> {
            resp.send(S.dump(req));
        }).use("/user", router);

        app.listen(8080);
    }

    public static void www() {
        Pond app = Pond.init().debug();
        app._static("www");
        app.get("/123", (req, resp) -> {
            resp.send("123");
        });
        app.listen(8080);
    }


    public static void tmpl() {
        Pond app = Pond.init().debug();
        app.get("/",(req, resp) -> {
            resp.render(Render.view("home.view"));
        });
        app.listen(8080);
    }

    public static void test_cross_mid_ctx_continuity() {
        Pond app = Pond.init().debug();
        Holder<Map> tester = new Holder<>();
        app.get("/",(req, resp) -> {
            req.ctx().put("a", "a");
            System.out.println(req.ctx());
            tester.val = req.ctx();
            req.ctx().put("id", req.ctx());
        });
        app.get("/", (req, resp) -> {
            System.out.println(req.ctx() == tester.val);

            resp.send(req.ctx().toString());
        });
        app.listen(8080);
    }

    public static void test_cross_mid_ctx_continuity_complex() {
        Pond app = Pond.init().debug();
        Holder<Map> tester = new Holder<>();
        app.get("/users",(req, resp ) -> {
            req.ctx().put("a", "a");
            System.out.println(req.ctx());
            tester.val = req.ctx();
            req.ctx().put("id", req.ctx());
            resp.send(req.ctx().toString());
        });
        app.before((req, resp, next) -> {
            req.ctx().put("val", 1);
            System.out.println ("here");
            next.apply();
        });
        app.listen(8080);
    }

    public static void test_min_group_route(){
        Pond app = Pond.init().debug();
        app.get("/${id}/new",(req,resp)->resp.send(req.param("id")+"/new1"));
        app.get("/new/${id}",(req,resp)->resp.send("new2/"+req.param("id")));
        app.get("/new",(req,resp)->resp.send("new"));
        app.get("/${id}",(req,resp)->resp.send("id="+req.param("id")));
        app.listen(8080);
    }

    public static void main(String[] args) {
//        basic();
//        router();
        www();
//        tmpl();
//        test_cross_mid_ctx_continuity_complex();
//        test_min_group_route();
    }
}
