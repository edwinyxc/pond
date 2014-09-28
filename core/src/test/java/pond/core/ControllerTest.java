package pond.core;

import pond.common.S;
import pond.core.http.HttpMethod;

public class ControllerTest extends Controller {

    @Mapping(value = "/doa",
            methods = {HttpMethod.POST})
    public void doA(Request req, Response resp) {
        resp.send("a");
    }

    @Mapping("/dob")
    public void doB(Request req, Response resp) {
        resp.send("b");
    }

    @Mapping
    public void c(Request req, Response resp){
        req.ctx().put("test", S.list.one("123333", "sss","2333","gf"));
        resp.render(Render.view("home.view"));
    }

    public static void main(String[] args) {
        Pond app = Pond.init().debug();
        app.use("/co", new ControllerTest());
        app.get("/co", (req,resp)->{
                   resp.send("here");
                });
        app.listen(8080);
    }
}