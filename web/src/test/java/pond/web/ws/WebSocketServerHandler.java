///*
// * Copyright 2012 The Netty Project
// *
// * The Netty Project licenses this file to you under the Apache License,
// * version 2.0 (the "License"); you may not use this file except in compliance
// * with the License. You may obtain a copy of the License at:
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
// * License for the specific language governing permissions and limitations
// * under the License.
// */
//package pond.web.ws;
//
//import io.netty.buffer.ByteBuf;
//import io.netty.buffer.Unpooled;
//import io.netty.channel.*;
//import io.netty.handler.codec.http.DefaultFullHttpResponse;
//import io.netty.handler.codec.http.FullHttpRequest;
//import io.netty.handler.codec.http.FullHttpResponse;
//import io.netty.handler.codec.http.websocketx.*;
//import io.netty.util.CharsetUtil;
//import pond.common.S;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//import static io.netty.handler.codec.http.HttpHeaderNames.*;
//import static io.netty.handler.codec.http.HttpHeaderUtil.isKeepAlive;
//import static io.netty.handler.codec.http.HttpHeaderUtil.setContentLength;
//import static io.netty.handler.codec.http.HttpMethod.GET;
//import static io.netty.handler.codec.http.HttpResponseStatus.*;
//import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
//
///**
// * Handles handshakes and messages
// */
//public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {
//    private static final Logger logger = Logger.getLogger(WebSocketServerHandler.class.getName());
//
//    private static final String WEBSOCKET_PATH = "/websocket";
//
//    private WebSocketServerHandshaker handshaker;
//
//    private static List<ChannelHandlerContext> ctxs= new ArrayList<>();
//
//    @Override
//    public void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
//        if (msg instanceof FullHttpRequest) {
//            handleHttpRequest(ctx, (FullHttpRequest) msg);
//        } else if (msg instanceof WebSocketFrame) {
//            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
//        }
//    }
//
//    @Override
//    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        ctx.flush();
//    }
//
//    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
//        // Handle a bad request.
//        if (!req.decoderResult().isSuccess()) {
//            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
//            return;
//        }
//
//        // Allow only GET methods.
//        if (req.method() != GET) {
//            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
//            return;
//        }
//
//        // Send the demo page and favicon.ico
//        if ("/".equals(req.uri())) {
//            ByteBuf content = WebSocketServerIndexPage.getContent(getWebSocketLocation(req));
//            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);
//
//            res.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
//            setContentLength(res, content.readableBytes());
//
//            sendHttpResponse(ctx, req, res);
//            return;
//        }
//        if ("/favicon.ico".equals(req.uri())) {
//            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
//            sendHttpResponse(ctx, req, res);
//            return;
//        }
//
//        // Handshake
//        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
//                getWebSocketLocation(req), null, false);
//        handshaker = wsFactory.newHandshaker(req);
//        if (handshaker == null) {
//            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
//        } else {
//            handshaker.handshake(ctx.channel(), req);
//            S.echo("Adding channel" + ctx);
//            ctxs.add(ctx);
//        }
//    }
//
//    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
//
//        // Check for closing frame
//        if (frame instanceof CloseWebSocketFrame) {
//            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
//            return;
//        }
//        if (frame instanceof PingWebSocketFrame) {
//            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
//            return;
//        }
//        if (!(frame instanceof TextWebSocketFrame)) {
//            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass()
//                    .getName()));
//        }
//
//        // Send the uppercase string back.
//        String request = ((TextWebSocketFrame) frame).text();
//        if (logger.isLoggable(Level.FINE)) {
//            logger.fine(String.format("%s received %s", ctx.channel(), request));
//        }
//        S._for(ctxs).each(c -> {
//            c.channel().write(new TextWebSocketFrame(request.toUpperCase()));
//            c.flush();
//        });
//    }
//
//    private static void sendHttpResponse(
//            ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
//        // Generate an error page if response getStatus code is not OK (200).
//        if (res.status().code() != 200) {
//            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
//            res.content().writeBytes(buf);
//            buf.release();
//            setContentLength(res, res.content().readableBytes());
//        }
//
//        // Send the response and close the connection if necessary.
//        ChannelFuture f = ctx.channel().writeAndFlush(res);
//        if (!isKeepAlive(req) || res.status().code() != 200) {
//            f.addListener(ChannelFutureListener.CLOSE);
//        }
//    }
//
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        cause.printStackTrace();
//        ctx.close();
//    }
//
//    private static String getWebSocketLocation(FullHttpRequest req) {
//        return "ws://" + req.headers().get(HOST) + WEBSOCKET_PATH;
//    }
//}
