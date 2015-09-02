package pond.web;

/**
 * Created by ed on 9/2/15.
 */
public class MaualTest {


  public static void main(String[] args){

    Pond app = Pond.init();

    app.before((req, resp) -> {
      req.ctx().put("val", 1);
    });

    app.get("/testCtx", (req, resp) -> {
      req.ctx().put("a", "a");
      resp.send(200, req.ctx().get("a").toString() + req.ctx().get("val"));
    });
    app.listen();
  }

}
