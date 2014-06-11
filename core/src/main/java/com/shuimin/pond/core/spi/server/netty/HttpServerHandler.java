package com.shuimin.pond.core.spi.server.netty;

import com.shuimin.common.S;
import com.shuimin.common.f.Callback;
import com.shuimin.pond.core.Request;
import com.shuimin.pond.core.Response;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpVersion;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Values.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

public class HttpServerHandler
        extends SimpleChannelInboundHandler<FullHttpRequest> {

    final Callback.C2<Request, Response> handler; // THE DISPATCHER

    public HttpServerHandler(Callback.C2<Request, Response>
                                     dispatcher) {
        this.handler = dispatcher;
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx,
                                   FullHttpRequest msg)
            throws Exception {
        FullHttpResponse f_resp
                = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, OK,
                Unpooled.buffer());

        try {
            handler.apply(new NettyRequest(msg, ctx.channel()),
                    new NettyResponse(f_resp, ctx));
//            MiddlewareContext.debug("after dispatch");
//            MiddlewareContext.debug(f_resp.getStatus());
//            MiddlewareContext.debug(f_resp.headers());
//            MiddlewareContext.debug(f_resp.content());

            if (isKeepAlive(msg)) {
                //length
                f_resp.headers().set(
                        CONTENT_LENGTH, f_resp.content().readableBytes());
                S.echo(f_resp.content());
                f_resp.headers().set(CONNECTION, KEEP_ALIVE);
            }
            ChannelFuture last = ctx.writeAndFlush(f_resp);
            // Write the end marker
            if (!isKeepAlive(msg)) {
                // Close the connection when the whole content is written out.
                last.addListener(ChannelFutureListener.CLOSE);
            }

        } finally {
            // Write the end marker
            ctx.channel().flush();
            ctx.flush();
        }
    }

}
