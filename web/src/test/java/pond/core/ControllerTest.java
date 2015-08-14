package pond.core;

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

  public static void main(String[] args) throws Exception {
    Pond app = Pond.init().debug();
    app.use("/co", new ControllerTest());
    app.get("/co", (req, resp) -> {
      resp.send("here");
    });
    app.listen();
  }
}