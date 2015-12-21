package pond.web;

import pond.common.S;
import pond.core.ExecutionContext;
import pond.web.http.HttpMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Execution Context, attached to a single thread.
 */
public class WebCtx extends ExecutionContext {

  final Request req;
  final Response resp;
  Pond pond;
  //original path
  final String path;
  final String uri;
  Route route;
  HttpMethod method;
  List<Mid> handledMids = new ArrayList<>();
  boolean handled = false;

  public WebCtx(Request req, Response resp, Pond pond) {
    super("system");
    this.resp = new ResponseWrapper(this, resp);
    req = (this.req = new RequestWrapper(this, req));
    this.path = req.path();
    this.uri = req.uri();
    this.pond = pond;

    S._debug(Pond.logger, log -> {
      log.debug("Main ctx route:");
      super.set("_start_time", S.now());
      log.debug("ctx starts at: " + this.get("_start_time"));

    });
  }

  public void put(String name, Object o) {
    super.set(name, o);
  }

  public boolean isHandled(Mid mid) {
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

  public WebCtx route(Route r) {
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
