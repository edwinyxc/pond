package pond.web;

/**
 * Put my mids here
 */
public class InternalMids {

  final public static Mid FORCE_CLOSE = (req, resp) -> {
    WebCtx _ctx = req.ctx();
    if (_ctx != null && !_ctx.handled) {
      resp.send(404);
    }
  };

}
