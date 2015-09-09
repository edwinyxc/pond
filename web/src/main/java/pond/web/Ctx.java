package pond.web;

import pond.common.S;

import java.util.HashMap;

/**
 * Execution Context, attached to a single thread.
 */
public class Ctx extends HashMap<String, Object> {

  Request req;
  Response resp;
  Pond pond;
  Route route;
  boolean handled = false;

  public Ctx(Request req, Response resp, Pond pond)
  {
    this.req = req;
    this.resp = new ResponseWrapper(resp);
    this.pond = pond;

    S._debug(Pond.logger, log -> {

      log.debug("Main ctx route:");
      this.put("_start_time", S.now());
      log.debug("ctx starts at: " + this.get("_start_time"));

    });

  }

  public Pond pond() {
    return pond;
  }

  public Route route() {
    return route;
  }

  public Ctx route(Route r) {
    route = r;
    return this;
  }

  void setHandled(boolean b) {
    this.handled = b;
  }

  public Request req() {
    return req;
  }

  public Response resp() {
    return resp;
  }


}
