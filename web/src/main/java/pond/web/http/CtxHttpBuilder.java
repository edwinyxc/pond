package pond.web.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;
import pond.common.S;
import pond.core.CtxBase;
import pond.net.CtxNet;
import pond.net.NetServer;
import pond.web.CtxHandler;

import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

class CtxHttpBuilder {

    final Iterable<CtxHandler> handlers;
    final HttpConfigBuilder configBuilder;
    CtxHttpBuilder(HttpConfigBuilder builder, Iterable<CtxHandler> handlers){
        configBuilder = builder;
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
    List<Callback.C3<CtxHttpBuilder, ChannelHandlerContext, HttpRequest>>
        on_HttpRequest = new ArrayList<>(){{
        add((builder, ctx, request) -> {

        });
    }};

    final
    List<Callback.C4<CtxHttpBuilder, ChannelHandlerContext, HttpRequest, QueryStringDecoder>>
        on_QueryString = new ArrayList<>(){{
        add((builder, ctx, request, decoder) -> {
            //uri-queries
            Map<String, List<String>> parsedParams = decoder.parameters();

        });
    }};
    final List<Callback.C4<CtxHttpBuilder, ChannelHandlerContext, HttpRequest, HttpContent>> on_HttpContents = new ArrayList<>();
    final List<Callback.C4<CtxHttpBuilder, ChannelHandlerContext, HttpRequest, HttpPostMultipartRequestDecoder>> on_Multipart= new ArrayList<>();
    final List<Callback.C4<CtxHttpBuilder, ChannelHandlerContext, HttpRequest, FileUpload>> on_Multipart_UploadFile = new ArrayList<>();
    final List<Callback.C4<CtxHttpBuilder, ChannelHandlerContext, HttpRequest, Attribute>> on_Multipart_Attribute = new ArrayList<>();
    final List<Callback.C4<CtxHttpBuilder, ChannelHandlerContext, HttpRequest, LastHttpContent>> on_LastHttpContent= new ArrayList<>();
    final List<Callback.C3<CtxHttpBuilder, ChannelHandlerContext, WebSocketFrame>> on_WebSocketFrames = new ArrayList<>();

    //out-bounds

    final List<Callback.C3<CtxHttpBuilder, ChannelHandlerContext, HttpResponse>> on_SendResponse = new ArrayList<>();
    final List<Callback.C4<CtxHttpBuilder, ChannelHandlerContext, HttpResponse, ByteBuf>> on_SendNormal = new ArrayList<>();
    final List<Callback.C3<CtxHttpBuilder, ChannelHandlerContext, HttpResponse>> on_SendUnhandled = new ArrayList<>();
    final List<Callback.C4<CtxHttpBuilder, ChannelHandlerContext, HttpResponse, RandomAccessFile>> on_SendFile = new ArrayList<>();
    final List<Callback.C3<CtxHttpBuilder, ChannelHandlerContext, Throwable >> on_ExceptionCaught = new ArrayList<>();


    public CtxHttpBuilder onHttpRequest(Callback.C3<CtxHttpBuilder, ChannelHandlerContext, HttpRequest> handler){
        on_HttpRequest.add(handler);
        return this;
    }
    public CtxHttpBuilder onHttpContent(Callback.C3<CtxHttpBuilder, ChannelHandlerContext, HttpContent> handler){
        on_HttpContents.add(handler);
        return this;
    }

    public CtxHttpBuilder onLastHttpRequest(Callback.C3<CtxHttpBuilder, ChannelHandlerContext, LastHttpContent> handler){
        on_LastHttpContent.add(handler);
        return this;
    }

    public CtxHttpBuilder onWebSocketFrames(Callback.C3<CtxHttpBuilder, ChannelHandlerContext, WebSocketFrame> handler){
        on_WebSocketFrames.add(handler);
        return this;
    }
    */

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
        ctx.write(response);
    }

    private void sendBadRequest(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new DefaultHttpResponse(HttpVersion.HTTP_1_1,
            HttpResponseStatus.BAD_REQUEST))
            .addListener(ChannelFutureListener.CLOSE);
    }

    public SimpleChannelInboundHandler<Object> build(){
        return new SimpleChannelInboundHandler<Object>() {

            HttpRequest httpRequest;
            HttpPostRequestDecoder postRequestDecoder;
            CompositeByteBuf aggregatedContent;
            Map<String, List<String>> queries;
            HttpHeaders trailingHeaders;
            boolean is_keepAlive;
            boolean is_multipart;

            private void clean(){
                httpRequest = null;
                aggregatedContent = null;
                queries = null;
                is_keepAlive = false;
                is_multipart = false;

                if(postRequestDecoder != null){
                    postRequestDecoder.cleanFiles();
                    postRequestDecoder.destroy();
                    postRequestDecoder = null;
                }
            }

            private void receiveHttpRequest(ChannelHandlerContext ctx, HttpRequest request){
                if (HttpUtil.is100ContinueExpected(request)) {
                    send100Continue(ctx);
                }
                clean();
                httpRequest = request;
                is_keepAlive = HttpUtil.isKeepAlive(request);
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
                            postRequestDecoder = new HttpPostRequestDecoder(factory, request);
                        } catch (Throwable e) {
                            NetServer.logger.error(e.getMessage(), e);
                            sendBadRequest(ctx);
                        }
                    }
                } else {
                    //aggregated
                    aggregatedContent = PooledByteBufAllocator.DEFAULT.compositeBuffer();
                }

                String uri = request.uri();
                //queries
                //TODO replace charset
                queries = new QueryStringDecoder(uri, CharsetUtil.UTF_8).parameters();

                S._debug(NetServer.logger, log -> {
                    log.debug("URI : " + request.uri());
                    log.debug("");
                });

                //TODO cookies
            }

            private void receiveHttpContent(ChannelHandlerContext ctx,
                                            HttpContent content){

                if(!content.decoderResult().isSuccess()) {
                    sendBadRequest(ctx);
                    return;
                }

                if(postRequestDecoder != null && is_multipart){
                    postRequestDecoder.offer(content);
                }

                //merge chunks -- retain at all cases
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

                //compose into fullCtx :)
                CtxBase base = new CtxBase();
                CtxHttp http = () -> base;
                CtxNet.adapt(http, ctx);
                http.set(CtxHttp.Keys.NettyRequest, httpRequest);
                http.set(CtxHttp.Keys.TrailingHeaders, trailingHeaders);
                http.set(CtxHttp.Keys.FromData, postRequestDecoder);
                http.set(CtxHttp.Keys.Builder, CtxHttpBuilder.this);
                http.set(CtxHttp.Keys.In, aggregatedContent);
                http.set(CtxHttp.Keys.Queries, queries);

                //inject handlers
                http.addHandlers(handlers);

                //Server processing CompletionFuture
                http.run();
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
                sendBadRequest(ctx);
                super.exceptionCaught(ctx, cause);
            }
        };
    }

}
