package pond.web.http;

import io.netty.buffer.*;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import pond.common.S;
import pond.common.STRING;
import pond.common.f.Tuple;
import pond.core.CtxBase;
import pond.net.CtxNet;
import pond.net.NetServer;
import pond.web.CtxHandler;
import pond.web.Request;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

class CtxHttpBuilder {

    final Iterable<CtxHandler> handlers;
    final HttpConfigBuilder httpConfig;
    CtxHttpBuilder(HttpConfigBuilder builder, Iterable<CtxHandler> handlers){
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
            List<FileUpload> mul_fileUploads;
            Map<String, List<String>> formData;
            HttpHeaders trailingHeaders;
            boolean is_keepAlive;
            boolean is_multipart;

            private void clean(){
                httpRequest = null;
                aggregatedContent = null;
                queries = null;
                is_keepAlive = false;
                is_multipart = false;
                formData = null;

                if(postRequestDecoder != null){
                    postRequestDecoder.cleanFiles();
                    postRequestDecoder.destroy();
                    postRequestDecoder = null;
                    mul_fileUploads = null;
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
                            formData = new LinkedHashMap<>();
                            mul_fileUploads = new LinkedList<>();
                        } catch (Throwable e) {
                            NetServer.logger.error(e.getMessage(), e);
                            sendBadRequest(ctx);
                        }
                    }
                }

                //aggregated
                aggregatedContent = PooledByteBufAllocator.DEFAULT.compositeBuffer();

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
                try {
                    if (postRequestDecoder != null && is_multipart) {
                        postRequestDecoder.offer(content);
                        for (InterfaceHttpData data = postRequestDecoder.next();
                             data != null && postRequestDecoder.hasNext();
                             data = postRequestDecoder.next()) {
//          // check if current HttpData is a FileUpload and previously set as partial
//          if (partialContent == data) {
//            S._debug(logger, log -> log.debug(" 100% (FinalSize: " + partialContent.length() + ")" + " 100% (FinalSize: " + partialContent.length() + ")"));
////            partialContent = null;
//          }
                            InterfaceHttpData.HttpDataType type = data.getHttpDataType();
                            switch (type) {
                                case Attribute: {
                                    Attribute attr = (Attribute) data;

                                    S._debug(NetServer.logger, log -> {
                                        try {
                                            log.debug("PARSE ATTR: " + attr.getName() + " : " + attr.getValue());
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    });
                                    HttpUtils.appendToMap(formData, attr.getName(), attr.getValue());
                                    break;
                                }
                                case FileUpload: {
                                    FileUpload fileUpload = (FileUpload) data;
                                    S._debug(NetServer.logger, log -> {
                                        try {
                                            log.debug("PARSE FILE: " + fileUpload.getName()
                                                          + " : " + fileUpload.getFilename()
                                                          + " : " + fileUpload.getFile().getAbsolutePath());
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    });
                                    mul_fileUploads.add(fileUpload);
                                    break;
                                }
                            }
                        }
                    }
                }catch (HttpPostRequestDecoder.EndOfDataDecoderException end){
                    //ignore
                }catch (Exception e){
                    NetServer.logger.error(e.getMessage(), e);
                    sendBadRequest(ctx);
                    clean();
                    return;
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
                //parse http finished

                //compose into fullCtx :)
                CtxBase base = new CtxBase();
                CtxHttp http = () -> base;
                CtxNet.adapt(http, ctx);
                http.set(CtxHttp.Keys.NettyRequest, httpRequest);
                http.set(CtxHttp.Keys.TrailingHeaders, trailingHeaders);
                http.set(CtxHttp.Keys.FormData, Tuple.pair(formData, mul_fileUploads));
                http.set(CtxHttp.Keys.Config, httpConfig);
                http.set(CtxHttp.Keys.Queries, queries);

                //in & out
                http.set(CtxHttp.Keys.In, aggregatedContent);
                http.set(CtxHttp.Keys.Out, PooledByteBufAllocator.DEFAULT.heapBuffer());
                var header_bind = (CtxHttp.Headers) http::bind;
                String content_type = header_bind.ContentType();

                //handle http-contents
                if(is_multipart) {
                    //has all consumed

                }else {
                    //treat as default application/x-www-urlencoded
                    if(content_type == null || content_type.toLowerCase().contains(HttpHeaderNames.CONTENT_TYPE.toLowerCase())) {
                        var s = aggregatedContent.toString(CharsetUtil.UTF_8);
                        new QueryStringDecoder(s, CharsetUtil.UTF_8);
                    }
                }


                //cookies

                //merge into one
                var cookie = httpRequest.headers().getAsString(HttpHeaderNames.COOKIE);
                if(STRING.notBlank(cookie)){
                    cookie += trailingHeaders.getAsString(HttpHeaderNames.COOKIE);
                }
                if(STRING.notBlank(cookie)){
                    var cookies = ServerCookieDecoder.STRICT.decode(cookie);
                    http.set(CtxHttp.Keys.HasCookie, true);
                    http.set(CtxHttp.Keys.Cookies, cookies);
                }

                //handle httpContent

                //req & resp
                Request request = new Request() {
                    Map<String, List<String>> _in_url_params;
                    @Override
                    public String method() {
                        return httpRequest.method().toString();
                    }

                    @Override
                    public String remoteIp() {
                        return ctx.channel().remoteAddress().toString();
                    }

                    @Override
                    public InputStream in() {
                        return new ByteBufInputStream(aggregatedContent);
                    }

                    @Override
                    public String uri() {
                        return httpRequest.uri();
                    }

                    @Override
                    public Map<String, List<String>> headers() {
                        var ctx = (CtxHttp.Headers)http::bind;
                        return ctx.all();
                    }

                    @Override
                    public Map<String, List<String>> queries() {
                        return queries;
                    }

                    @Override
                    public Map<String, List<String>> inUrlParams() {
                        return _in_url_params;
                    }

                    @Override
                    public Map<String, List<String>> formData() {
                        return formData;
                    }

                    @Override
                    public Map<String, List<UploadFile>> files() {
                        return null;
                    }

                    @Override
                    public Map<String, Cookie> cookies() {
                        return null;
                    }

                    @Override
                    public String path() {
                        return ;
                    }

                    @Override
                    public CtxHttp ctx() {
                        return http;
                    }
                };


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
