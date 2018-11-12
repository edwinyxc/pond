package pond.web;

import pond.common.f.Callback;
import pond.core.CtxHandler;
import pond.web.http.HttpCtx;

/**
 * A
 */
public interface Mid extends Callback.C2<Request, Response> {

  final static Mid NOOP = (req, resp) -> {
  };

  static Mid of(Callback.C2<Request, Response> c) {
    return (Mid) c;
  }

  default CtxHandler<HttpCtx> toCtxHandler() {
    return ctx -> {
      var lazy = (HttpCtx.Lazy) ctx::bind;
      this.apply(lazy.req(), lazy.resp());
    };
  }


}
