package pond.web.http;

import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;
import pond.common.S;
import pond.core.Ctx;
import pond.core.CtxBase;
import pond.core.CtxHandler;
import pond.net.CtxNet;
import pond.net.NetServer;
import pond.web.EndToEndException;

import java.net.URI;
import java.util.*;

import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

class HttpCtxBuilder {

    final Iterable<CtxHandler<? extends Ctx>> handlers;
    final HttpConfigBuilder httpConfig;
    HttpCtxBuilder(HttpConfigBuilder builder, Iterable<CtxHandler<? extends Ctx>> handlers){
        httpConfig = builder;
        this.handlers = handlers;
    }

    static HttpDataFactory factory;
    static{
        /**
         * For UploadFile
         */
        DiskFileUpload.deleteOnExitTemporaryFile = true; // should delete file
        // on exit (in normal // exit)
        DiskFileUpload.baseDirectory = null; // system temp directory
        DiskAttribute.deleteOnExitTemporaryFile = true; // should delete file on
        // exit (in normal exit)
        DiskAttribute.baseDirectory = null; // system temp directory

        /**
         * For HttpDataFactory
         */
        factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MAXSIZE); // Disk if size exceed
    }


    /***
    http parser aspect-callbacks
    ***/

    /*
    public

    //in-bounds
    final
    List<Callback.C3<HttpCtxBuilder, ChannelHandlerContext, HttpRequest>>
        on_HttpRequest = new ArrayList<>(){{
        add((builder, ctx, request) -> {

        });
    }};

    final
    List<Callback.C4<HttpCtxBuilder, ChannelHandlerContext, HttpRequest, QueryStringDecoder>>
        on_QueryString = new ArrayList<>(){{
        add((builder, ctx, request, decoder) -> {
            //uri-queries
            Map<String, List<String>> parsedParams = decoder.parameters();

        });
    }};
    final List<Callback.C4<HttpCtxBuilder, ChannelHandlerContext, HttpRequest, HttpContent>> on_HttpContents = new ArrayList<>();
    final List<Callback.C4<HttpCtxBuilder, ChannelHandlerContext, HttpRequest, HttpPostMultipartRequestDecoder>> on_Multipart= new ArrayList<>();
    final List<Callback.C4<HttpCtxBuilder, ChannelHandlerContext, HttpRequest, FileUpload>> on_Multipart_UploadFile = new ArrayList<>();
    final List<Callback.C4<HttpCtxBuilder, ChannelHandlerContext, HttpRequest, Attribute>> on_Multipart_Attribute = new ArrayList<>();
    final List<Callback.C4<HttpCtxBuilder, ChannelHandlerContext, HttpRequest, LastHttpContent>> on_LastHttpContent= new ArrayList<>();
    final List<Callback.C3<HttpCtxBuilder, ChannelHandlerContext, WebSocketFrame>> on_WebSocketFrames = new ArrayList<>();

    //out-bounds

    final List<Callback.C3<HttpCtxBuilder, ChannelHandlerContext, HttpResponse>> on_SendResponse = new ArrayList<>();
    final List<Callback.C4<HttpCtxBuilder, ChannelHandlerContext, HttpResponse, ByteBuf>> on_SendNormal = new ArrayList<>();
    final List<Callback.C3<HttpCtxBuilder, ChannelHandlerContext, HttpResponse>> on_SendUnhandled = new ArrayList<>();
    final List<Callback.C4<HttpCtxBuilder, ChannelHandlerContext, HttpResponse, RandomAccessFile>> on_SendFile = new ArrayList<>();
    final List<Callback.C3<HttpCtxBuilder, ChannelHandlerContext, Throwable >> on_ExceptionCaught = new ArrayList<>();


    public HttpCtxBuilder onHttpRequest(Callback.C3<HttpCtxBuilder, ChannelHandlerContext, HttpRequest> handler){
        on_HttpRequest.add(handler);
        return this;
    }
    public HttpCtxBuilder onHttpContent(Callback.C3<HttpCtxBuilder, ChannelHandlerContext, HttpContent> handler){
        on_HttpContents.add(handler);
        return this;
    }

    public HttpCtxBuilder onLastHttpRequest(Callback.C3<HttpCtxBuilder, ChannelHandlerContext, LastHttpContent> handler){
        on_LastHttpContent.add(handler);
        return this;
    }

    public HttpCtxBuilder onWebSocketFrames(Callback.C3<HttpCtxBuilder, ChannelHandlerContext, WebSocketFrame> handler){
        on_WebSocketFrames.add(handler);
        return this;
    }
    */

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
        ctx.write(response);
    }

    private ChannelFuture sendBadRequest(ChannelHandlerContext ctx) {
        return ctx.writeAndFlush(new DefaultHttpResponse(HttpVersion.HTTP_1_1,
            HttpResponseStatus.BAD_REQUEST))
            .addListener(ChannelFutureListener.CLOSE);
    }

    private ChannelFuture sendInternalError(ChannelHandlerContext ctx, Throwable e) {
        return ctx.writeAndFlush(
            new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
            HttpResponseStatus.INTERNAL_SERVER_ERROR,
            (e instanceof EndToEndException)
                ? Unpooled.wrappedBuffer(String.valueOf(((EndToEndException) e).message).getBytes())
                : Unpooled.wrappedBuffer(String.valueOf(e.getMessage()).getBytes())
            )).addListener(ChannelFutureListener.CLOSE);
    }

    static void unwrapRuntimeException(HttpCtx ctx, RuntimeException e) {

        var send = (HttpCtx.Send)ctx::bind;
        if (e instanceof EndToEndException) {
            EndToEndException ete = (EndToEndException) e;
            HttpCtx.logger.warn("EEE:"
                           + ((EndToEndException) e).http_status
                           + ":" + e.getMessage(), e);
            send.send(new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.valueOf(ete.http_status),
                Unpooled.wrappedBuffer(String.valueOf(ete.message).getBytes(CharsetUtil.UTF_8))
                ));
            return;
        }

        Throwable t = e.getCause();
        if (t == null) {
            HttpCtx.logger.error(e.getMessage(), e);
            send.send(new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                INTERNAL_SERVER_ERROR,
                (e.getMessage() != null)
                    ? Unpooled.wrappedBuffer(e.getMessage().getBytes(CharsetUtil.UTF_8))
                    : Unpooled.wrappedBuffer("null".getBytes(CharsetUtil.UTF_8))
            ));
            return;
        }

        if (t instanceof RuntimeException) {
            unwrapRuntimeException(ctx, (RuntimeException) t);
        } else {
            HttpCtx.logger.error(t.getMessage(), t);
            send.send(new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                INTERNAL_SERVER_ERROR,
                Unpooled.wrappedBuffer(String.valueOf(e.getMessage()).getBytes(CharsetUtil.UTF_8))
            ));
        }
    }

    public SimpleChannelInboundHandler<Object> build(){
        return new SimpleChannelInboundHandler<Object>() {

            HttpRequest httpRequest;
            CompositeByteBuf aggregatedContent;
            Map<String, List<String>> queries;
            HttpCtx.Body.FormData formData;
            HttpHeaders trailingHeaders;
            String path;


            boolean is_multipart;

            private void clean(){
                httpRequest = null;
                aggregatedContent = null;
                queries = null;
                is_multipart = false;
                if(formData != null){
                    formData.close();
                    formData = null;
                }
            }

            private void receiveHttpRequest(ChannelHandlerContext ctx, HttpRequest request){
                if (HttpUtil.is100ContinueExpected(request)) {
                    send100Continue(ctx);
                }
                clean();
                httpRequest = request;
                is_multipart = HttpPostRequestDecoder.isMultipart(request);

                S._debug(NetServer.logger, log -> {
                    log.debug("GOT HTTP REQ: ");
                    log.debug(request.toString());
                });

                if(!request.decoderResult().isSuccess()){
                    sendBadRequest(ctx);
                }

                if(is_multipart) {
                    HttpMethod method = request.method();
                    if(S._in(method, HttpMethod.PATCH, HttpMethod.PUT, HttpMethod.POST)){
                        try {
                            formData = new HttpCtx.Body.FormData(new HttpPostRequestDecoder(factory, request));
                        } catch (Throwable e) {
                            NetServer.logger.error(e.getMessage(), e);
                            sendBadRequest(ctx);
                        }
                    }
                }

                //aggregated
                aggregatedContent = PooledByteBufAllocator.DEFAULT.compositeBuffer();

                String uri = request.uri();
                path = S._try_ret(() -> new URI(uri).getPath());
                //queries
                //TODO replace charset
                queries = new QueryStringDecoder(uri, CharsetUtil.UTF_8).parameters();

                S._debug(NetServer.logger, log -> {
                    log.debug("URI : " + request.uri());
                    log.debug("");
                });
            }

            private void receiveHttpContent(ChannelHandlerContext ctx,
                                            HttpContent content){

                if(!content.decoderResult().isSuccess()) {
                    sendBadRequest(ctx);
                    return;
                }
                try {
                    if (formData != null && is_multipart) {
                        formData.offer(content);

                    }
                }catch (Exception e){
                    NetServer.logger.error(e.getMessage(), e);
                    sendBadRequest(ctx);
                    clean();
                    return;
                }

                //merge chunks -- retain at headers cases
                ByteBuf chunk = content.content();
                assert aggregatedContent != null;
                if(chunk.isReadable()) {
                    chunk.retain();
                    aggregatedContent.addComponent(chunk);
                    int cur_writer = aggregatedContent.writerIndex();
                    aggregatedContent.writerIndex(cur_writer + chunk.readableBytes());
                }

                if(content instanceof LastHttpContent){
                    LastHttpContent tail = (LastHttpContent) content;
                    if(!tail.decoderResult().isSuccess()) {
                        sendBadRequest(ctx);
                        return;
                    }

                    //trailing headers
                    trailingHeaders = tail.trailingHeaders();
                }

                /*****************************
                * http parsing finished here
                *****************************/

                //compose into fullCtx :)
                CtxBase base = new CtxBase();
                var http = (HttpCtx & HttpCtx.Lazy)() -> base;
                CtxNet.adapt(http, ctx);
                http.set(HttpCtx.NETTY_REQUEST, httpRequest);
                http.set(HttpCtx.TRAILING_HEADERS, trailingHeaders);
                http.set(HttpCtx.FORM_DATA, formData);
                http.set(HttpCtx.CONFIG, httpConfig);
                http.set(HttpCtx.Queries, queries);

                //in & out
                http.set(HttpCtx.IN, aggregatedContent);
                http.set(HttpCtx.OUT, PooledByteBufAllocator.DEFAULT.heapBuffer());

                //REQ & RESP
                http.set(HttpCtx.REQ, http.req());
                http.set(HttpCtx.RESP, http.resp());

                //inject handlers
                http.pushAll(handlers);
                http.flowProcessor().finalHandler(CtxHandler.of(c -> {
                    var finalHttp = (HttpCtx & HttpCtx.Send) c::bind;
                    var builder = finalHttp.get(HttpCtx.RESPONSE_BUILDER);
                    HttpRequest request = finalHttp.request();
                    // Decide whether to close the connection or not.
                    boolean close = request.headers() .contains(
                        HttpHeaderNames.CONNECTION,
                        HttpHeaderValues.CLOSE,
                        true
                    ) || request.protocolVersion().equals(
                        HttpVersion.HTTP_1_0
                    ) && !request.headers().contains(
                        HttpHeaderNames.CONNECTION,
                        HttpHeaderValues.KEEP_ALIVE,
                        true
                    );
                    if( builder != null ){
                        FullHttpResponse response = builder.build();
                        // Write the response.
                        ChannelFuture future = finalHttp.chctx().writeAndFlush(response);
                        // Close the connection after the write operation is done if necessary.
                        if (close) {
                            NetServer.logger.debug("Ready to close channel");
                            future.addListener(ChannelFutureListener.CLOSE);
                        }
                    }
                    else {
                        ctx.flush();
                        ctx.close();
                    }
                })).errorHandler(e -> {
                    if(e instanceof RuntimeException){
                        unwrapRuntimeException(http, (RuntimeException) e);
                    }
                    e.printStackTrace();
                    //sendInternalError(http.chctx(), e);
                    clean();
                });


                //Server processing CompletionFuture
                 http.runReactiveFlow(Ctx.ReactiveFlowConfig.DEFAULT);
            }


            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                super.channelActive(ctx);
            }

            @Override
            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                super.channelInactive(ctx);
            }

            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
                if(msg instanceof HttpRequest){
                    receiveHttpRequest(ctx, (HttpRequest)msg);
                } else if(msg instanceof HttpContent) {
                    if(httpRequest == null){
                        ctx.fireExceptionCaught(
                            new RuntimeException("HttpContent received without HttpRequest ...dropped"
                            + ((HttpContent) msg).content().toString(CharsetUtil.UTF_8))
                        );
                    } else receiveHttpContent(ctx, (HttpContent)msg);
                } else if(msg instanceof WebSocketFrame) {
                    WebSocketFrame frame = (WebSocketFrame) msg;
                    //TODO
                } else {
                    //unknown read
                    ctx.fireExceptionCaught(
                        new RuntimeException("Unknown msg received")
                    );
                }
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                clean();
                sendInternalError(ctx, cause);
                super.exceptionCaught(ctx, cause);
            }
        };
    }

}
