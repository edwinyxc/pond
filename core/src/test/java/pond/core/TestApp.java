package pond.core;

import pond.common.S;
import pond.common.f.Holder;
import pond.core.spi.server.netty.NettyReqWrapper;

import java.util.Map;

import static java.lang.Integer.parseInt;
import static pond.common.S.file.loadProperties;

/**
 * Created by ed on 7/9/14.
 */
public class TestApp {

    public static void basic() {
        Pond app = Pond.init();
//        app.get("/",(req, res ) -> {
//            req.param("user", "hellosdddaweqweqw" +
//                    "asdasdasdasd" +
//                    "asdqweqwe1231231321");
//            S.echo("####REACH");
//        });
        S._debug_on(NettyReqWrapper.class);
        app.get("/", (req, res) -> {
                    req.param("user", "1");
                    //next.apply();
                },
                (req, res) ->{
                    req.param("user", String.valueOf(parseInt(req.param("user"))+ 1));
                },
                (req, res) ->{
                    req.param("user", String.valueOf(parseInt(req.param("user"))+ 1));
                },
                (req, res) ->{
                    req.param("user", String.valueOf(parseInt(req.param("user"))+ 1));
                },
                (req, res) ->{
                    req.param("user", String.valueOf(parseInt(req.param("user"))+ 1));
                },
                (req, res) ->{
                    req.param("user", String.valueOf(parseInt(req.param("user"))+ 1));
                },
                (req, res) -> {
                    String user = req.param("user");
                    res.contentType("application/json");
                    res.send( user );
                });
        app.listen();
    }

    public static void config() {
        Pond app = Pond.init(p ->
                p.loadConfig(loadProperties("pond.conf")));

        app.get("/123", (req, resp) ->
                resp.send("<p>" + req.ctx().pond.attr("test") + "</p>"));

        app.listen();
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

        app.listen();
    }

    public static void www() {
        Pond app = Pond.init().debug();
        app._static("www");
        app.get("/123", (req, resp) -> {
            resp.send("123");
        });
        app.listen();
    }


    public static void tmpl() {
        Pond app = Pond.init().debug();
        app.get("/", (req, resp) -> {
            resp.render(Render.view("home.view"));
        });
        app.listen();
    }

    public static void test_cross_mid_ctx_continuity() {
        Pond app = Pond.init().debug();
        Holder<Map> tester = new Holder<>();
        app.get("/", (req, resp) -> {
            req.ctx().put("a", "a");
            System.out.println(req.ctx());
            tester.val = req.ctx();
            req.ctx().put("id", req.ctx());
        });
        app.get("/", (req, resp) -> {
            System.out.println(req.ctx() == tester.val);

            resp.send(req.ctx().toString());
        });
        app.listen();
    }

    public static void test_cross_mid_ctx_continuity_complex() {
        Pond app = Pond.init().debug();
        Holder<Map> tester = new Holder<>();
        app.get("/users", (req, resp) -> {
            req.ctx().put("a", "a");
            System.out.println(req.ctx());
            tester.val = req.ctx();
            req.ctx().put("id", req.ctx());
            resp.send(req.ctx().toString());
        });
        app.before((req, resp ) -> {
            req.ctx().put("val", 1);
        });
        app.listen();
    }

    public static void test_min_group_route() {
        Pond app = Pond.init().debug();
        app.get("/${id}/new", (req, resp) -> resp.send(req.param("id") + "/new1"));
        app.get("/new/${id}", (req, resp) -> resp.send("new2/" + req.param("id")));
        app.get("/new", (req, resp) -> resp.send("new"));
        app.get("/${id}", (req, resp) -> resp.send("id=" + req.param("id")));
        app.listen();
    }

    public static void main(String[] args) {
       basic();
//        config();
//        router();
//        www();
//        tmpl();
//        test_cross_mid_ctx_continuity_complex();
//        test_min_group_route();
    }
}
