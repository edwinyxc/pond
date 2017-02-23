package pond.web;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import pond.common.S;
import pond.common.f.Callback;

/**
 * Put my mids here
 */
public class InternalMids {

  final public static Mid FORCE_CLOSE = (req, resp) -> {
    HttpCtx _ctx = req.ctx();
    if (_ctx != null && !_ctx.handled) {
      resp.send(404);
    }
  };

  public static CtxHandler websocket(Callback<WSCtx> cb) {
    return _ctx -> {
      HttpCtx ctx = (HttpCtx) _ctx;
      // Handshake
      WebSocketServerHandshakerFactory wsFactory =
          new WebSocketServerHandshakerFactory(
          "ws://" + ctx.nettyRequest.headers().get(HttpHeaderNames.HOST)
              + ctx.path, null, false);

      WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(ctx.nettyRequest);
      if (handshaker == null) {
        ctx.send_type = HttpCtx.SEND_ERROR;
        WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.context.channel());
      } else {
        //build DefaultFullHttpRequest
        HttpRequest request = ctx.nettyRequest;
        S.dump("###req-headers:" + request.headers());
        FullHttpRequest fullreq =
            new DefaultFullHttpRequest(request.protocolVersion(),
                                       request.method(),
                                       request.uri(),
                                       ctx.inboundByteBuf,
                                       true);
        S._for(request.headers().entries()).each(l -> {
          fullreq.headers().add(l.getKey(), l.getValue());
        });

        handshaker.handshake(ctx.context.channel(), fullreq);
        WSCtx wsCtx = new WSCtx(ctx, handshaker);
        NettyHttpHandler.channelRegister.put(ctx.context.channel(), wsCtx);
        //build new Context
        cb.apply(wsCtx);
        //fire open event
        wsCtx.onOpenHandler.apply(wsCtx);
        //close this http request
        //set upgrade flag
        ctx.send_type = HttpCtx.SEND_UPGRADE_TO_WEBSOCKET;
        ctx.setHandled();
      }
    };
  }

}
