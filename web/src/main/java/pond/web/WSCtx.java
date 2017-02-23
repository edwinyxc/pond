package pond.web;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import pond.common.f.Callback;

/**
 * Created by ed on 21/02/17.
 */
public class WSCtx extends HttpCtx{

  WebSocketServerHandshaker handshaker;

  Callback.C2<String, WSCtx> onMessageHandler = (msg, ctx) -> {};
  Callback<WSCtx> onOpenHandler =  ctx -> {};
  Callback<WSCtx> onCloseHandler = (ctx)-> {};

  public WSCtx(HttpCtx ctx, WebSocketServerHandshaker handshaker) {
    super(ctx.nettyRequest, ctx.context, ctx.isKeepAlive, ctx.isMultipart, ctx.inboundByteBuf);
    this.handshaker = handshaker;
  }

  public void onMessage(Callback.C2<String, WSCtx> handler){
      onMessageHandler = handler;
  }

  public void onOpen(Callback<WSCtx> handler){
    onOpenHandler = handler;
  }

  public void onClose(Callback<WSCtx> handler){
    onCloseHandler = handler;
  }

  /**
   * 暂时不考虑半包、黏包和数据过长问题
   * @param msg
   */
  public void sendTextFrame(String msg){
    this.context.writeAndFlush(new TextWebSocketFrame(msg));
  }

  /**
   * 暂时不考虑半包、黏包和数据过长问题
   * @param byteBuf
   */
  public void sendBinaryFrame(ByteBuf byteBuf){
    this.context.writeAndFlush(new BinaryWebSocketFrame(byteBuf));
  }
}
