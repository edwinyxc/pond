package pond.web;

import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import pond.common.f.Callback;
import pond.common.f.Function;

/**
 * Created by ed on 21/02/17.
 */
public class WSCtx extends HttpCtx{

  WebSocketServerHandshaker handshaker;

  Callback.C2<String, WSCtx> onMessageHandler = (msg, ctx) -> {};
  Function<String, String> onOpenHandler = Function.EMPTY;
  Function.F0<String> onCloseHandler = ()-> "socket closed";

  public WSCtx(HttpCtx ctx, WebSocketServerHandshaker handshaker) {
    super(ctx.nettyRequest, ctx.context, ctx.isKeepAlive, ctx.isMultipart, ctx.inboundByteBuf);
    this.handshaker = handshaker;
  }

  public void onMessage(Callback.C2<String, WSCtx> handler){
      onMessageHandler = handler;
  }

  public void onOpen(Function<String, String> handler){
    onOpenHandler = handler;
  }

  public void onClose(Function.F0<String> handler){
    onCloseHandler = handler;
  }
}
