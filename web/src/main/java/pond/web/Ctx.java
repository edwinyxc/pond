package pond.web;

import pond.common.S;
import pond.web.http.HttpMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Execution Context, attached to a single thread.
 */
public class Ctx extends HashMap<String, Object> {

  Request req;
  Response resp;
  Pond pond;
  String path;
  String uri;
  Route route;
  HttpMethod method;
  List<Mid> handledMids = new ArrayList<>();
  boolean handled = false;

  public Ctx(Request req, Response resp, Pond pond) {
    this.req = req;
    this.path = req.path();
    this.uri = req.uri();
    this.resp = new ResponseWrapper(resp);
    this.pond = pond;

    S._debug(Pond.logger, log -> {

      log.debug("Main ctx route:");
      this.put("_start_time", S.now());
      log.debug("ctx starts at: " + this.get("_start_time"));

    });

  }

  public boolean alreadyHandled(Mid mid) {
    return handledMids.contains(mid);
  }

  public List<Mid> handledMids() {
    return handledMids;
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

  public String uri() {
    return uri;
  }

  public String path() {
    return path;
  }

  public HttpMethod method() {
    return method;
  }

}
