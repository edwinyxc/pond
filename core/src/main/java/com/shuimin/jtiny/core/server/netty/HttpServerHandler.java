package com.shuimin.jtiny.core.server.netty;

import com.shuimin.jtiny.core.Interrupt;
import com.shuimin.jtiny.core.RequestHandler;
import com.shuimin.jtiny.core.Server.G;
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

    final RequestHandler handler; // THE DISPATCHER

    public HttpServerHandler(RequestHandler dispatcher) {
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
            handler.handle(new NettyRequest(msg, ctx.channel()),
                new NettyResponse(f_resp,ctx));
//            MiddlewareContext.debug("after dispatch");
//            MiddlewareContext.debug(f_resp.getStatus());
//            MiddlewareContext.debug(f_resp.headers());
//            MiddlewareContext.debug(f_resp.content());

            if (isKeepAlive(msg)) {
                //length
                f_resp.headers().set(
                    CONTENT_LENGTH, f_resp.content().readableBytes());
                f_resp.headers().set(CONNECTION, KEEP_ALIVE);
            }
            ChannelFuture last = ctx.write(f_resp);
            // Write the end marker
            if (!isKeepAlive(msg)) {
                // Close the connection when the whole content is written out.
                last.addListener(ChannelFutureListener.CLOSE);
            }

        } catch (Interrupt.JumpInterruption jump) {
            //catch jump 
            G.debug(jump);
            G.debug(f_resp.getStatus());
            G.debug(f_resp.headers());
            ctx.writeAndFlush(f_resp).addListener(ChannelFutureListener.CLOSE);
        } finally {
            // Write the end marker
            ctx.flush();
        }
    }

}
