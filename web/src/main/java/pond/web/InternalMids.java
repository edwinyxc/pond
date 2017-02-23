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
            ctx.send_type = HttpCtx.SEND_UPGRADE_TO_WEBSOCKET;
            ctx.setHandled();
            ctx.put(NettyHttpHandler.WEBSOCKET_HANDLER_LABEL, cb);
        };
    }
}
