package pond.web;

/**
 * Put my mids here
 */
public class Mids {

  final static Mid FORCE_CLOSE = (req, resp) -> {
    Ctx _ctx = req.ctx();
    if (_ctx != null && !_ctx.handled) {
      resp.send(404);
    }
  };

}
