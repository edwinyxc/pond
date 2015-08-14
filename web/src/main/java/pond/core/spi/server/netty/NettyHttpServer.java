package pond.core.spi.server.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;
import pond.common.S;
import pond.common.STRING;
import pond.common.f.Tuple;
import pond.core.Response;
import pond.core.http.Cookie;
import pond.core.http.HttpUtils;
import pond.core.spi.BaseServer;
import pond.core.spi.server.AbstractServer;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class NettyHttpServer extends AbstractServer {

    static {
        //TODO config
        DiskFileUpload.deleteOnExitTemporaryFile = true; // should delete file
        // on exit (in normal // exit)
        DiskFileUpload.baseDirectory = null; // system temp directory
        DiskAttribute.deleteOnExitTemporaryFile = true; // should delete file on
        // exit (in normal exit)
        DiskAttribute.baseDirectory = null; // system temp directory
    }


    private static final HttpDataFactory factory =
            new DefaultHttpDataFactory(DefaultHttpDataFactory.MAXSIZE); // Disk if size exceed

    //    //executorServices -- for user threads
    private ExecutorService executorService = Executors.newCachedThreadPool();

    public NettyHttpServer() {

    }

    // configuration getters
    private boolean ssl() {
        return S._tap(Boolean.TRUE.equals(env(BaseServer.SSL)), b -> {
            if (b) {
                //TODO
                logger.warn("SSL is not supported");
                //logger.info("USING SSL");
            }
        });
    }

    private int port() {
        return S._tap(ssl() ? 443 : Integer.parseInt((String) S.avoidNull(env(BaseServer.PORT), "8080")),
                port -> logger.info(String.format("USING PORT %s", port)));
    }

    private int backlog() {
        return S._tap(Integer.parseInt((String) S.avoidNull(env(BaseServer.BACK_LOG), "128")),
                backlog -> logger.info(String.format("USING BACKLOG %s", backlog)));
    }

    private boolean keepAlive() {
        return S._tap(Boolean.TRUE.equals(env("keepAlive")),
                b -> {
                    if (b) logger.info("USING keepAlive");
                });
    }

    class NettyHttpHandler extends SimpleChannelInboundHandler<Object> {

        private HttpData partialContent;

        private HttpPostRequestDecoder decoder;

        HttpRequest httpRequest = null;

        NettyReqWrapper reqWrapper = null;

        CompositeByteBuf content;

        String contentType;

        private void sendBadRequest(ChannelHandlerContext ctx) {
            ctx.writeAndFlush(new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.BAD_REQUEST));
        }

        @Override
        protected void messageReceived(ChannelHandlerContext ctx, Object msg) {

            if (msg instanceof HttpRequest) {
                HttpRequest request = (HttpRequest) msg;

                S._debug(logger, log -> {
                    log.debug("GOT HTTP REQUEST:");
                    log.debug(request.toString());
                });

                if (HttpHeaderUtil.is100ContinueExpected(request)) {
                    ctx.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                            HttpResponseStatus.CONTINUE));
                }

                if (!request.decoderResult().isSuccess()) {
                    sendBadRequest(ctx);
                    return;
                }

                //do the initialize

                httpRequest = request;
                //build the req
                reqWrapper = new NettyReqWrapper(ctx, httpRequest);

                //parse headers
                reqWrapper.updateHeaders(headers ->
                                S._for(httpRequest.headers()).each(entry ->
                                                HttpUtils.appendToMap(headers, entry.getKey().toString(),
                                                        httpRequest.headers().getAllAndConvert(entry.getKey()))
                                )
                );

                //uri-queries
                Map<String, List<String>> parsedParams = new QueryStringDecoder(httpRequest.uri()).parameters();

                S._debug(logger, log -> {
                    log.debug("QUERY STRING: " + httpRequest.uri());
                    log.debug("PARSED PARAMS: " + S.dump(parsedParams));
                });

                reqWrapper.updateParams(params -> params.putAll(parsedParams));

                //parse cookies
                reqWrapper.updateCookies(
                        cookies ->
                                S._for(ServerCookieDecoder.decode(S.avoidNull(request.headers().getAndConvert(HttpHeaderNames.COOKIE), "")))
                                        .each(cookie -> S._tap(new Cookie(cookie.name(), cookie.value()),
                                                        c -> {
                                                            c.setSecure(cookie.isSecure());
                                                            c.setPath(cookie.path());
                                                            c.setVersion(cookie.version());
                                                            c.setMaxAge((int) cookie.maxAge());
                                                            c.setHttpOnly(cookie.isHttpOnly());
                                                            if (STRING.notBlank(cookie.domain()))
                                                                c.setDomain(cookie.domain());
                                                            c.setComment(cookie.comment());
                                                        })
                                        )

                );


                contentType = httpRequest.headers().getAndConvert(HttpHeaderNames.CONTENT_TYPE);
                //TODO test the raw "multipart"
                //build the multipart decoder
                if (HttpPostRequestDecoder.isMultipart(httpRequest)) {
                    HttpMethod method = httpRequest.method();

                    if (method.equals(HttpMethod.POST)
                            || method.equals(HttpMethod.PUT)
                            || method.equals(HttpMethod.PATCH)) {
                        try {
                            decoder = new HttpPostRequestDecoder(factory, request);
                            decoder.isMultipart();
                        } catch (HttpPostRequestDecoder.ErrorDataDecoderException err) {
                            logger.error(err.getMessage(), err);
                            sendBadRequest(ctx);
                            return;
                        }
                    } else {
                        logger.error("unexpected multipart request caught : invalid http method: " + method);
                        sendBadRequest(ctx);
                        return;
                    }
                } else {
                    S._assert(content == null);
                    content = Unpooled.compositeBuffer();
                }
            }

            if (msg instanceof HttpContent) {
                HttpContent httpContent = (HttpContent) msg;

                if (!httpContent.decoderResult().isSuccess()) {
                    sendBadRequest(ctx);
                    return;
                }

                if (httpRequest == null) {
                    ctx.fireExceptionCaught(new NullPointerException("httpRequest"));
                    return;
                }

                //multipart
                if (decoder != null && HttpPostRequestDecoder.isMultipart(httpRequest)) {
                    try {
                        Tuple<List<Attribute>, List<FileUpload>>
                                tuple = decodeHttpContent(decoder, httpContent);

                        reqWrapper.updateUploadFiles(files ->
                                        S._for(tuple._b).each(fileUpload ->
                                                        HttpUtils.appendToMap(files,
                                                                fileUpload.getName(),
                                                                new NettyUploadFile(fileUpload))
                                        )
                        );

                        reqWrapper.updateParams(params ->
                                        S._for(tuple._a).each(attr -> {
                                            String k = attr.getName();
                                            String v = S._try_ret(attr::getValue);
                                            S._debug(logger, log -> log.debug(k + " " + S.dump(v)));
                                            HttpUtils.appendToMap(params, k, v);
                                        })
                        );

                    } catch (Exception e1) {
                        logger.error(e1.getMessage(), e1);
                        S._debug(logger, log -> log.debug(e1.getMessage(), e1));
                        sendBadRequest(ctx);
                        return;
                    }

                } else {
                    //merge chunks
                    ByteBuf chunk = httpContent.content();
                    if (chunk.isReadable()) {
                        chunk.retain();
                        content.addComponent(httpContent.content());
                        content.writerIndex(content.writerIndex() + chunk.readableBytes());
                    }
                }

                //end of message
                if (msg instanceof LastHttpContent) {

                    //merge trailing headers
                    LastHttpContent trailer = (LastHttpContent) msg;
                    if (!trailer.decoderResult().isSuccess()) {
                        ctx.writeAndFlush(new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                                HttpResponseStatus.BAD_REQUEST));
                        return;
                    }

                    //trailing headers
                    if (!trailer.trailingHeaders().isEmpty()) {
                        for (CharSequence name : trailer.trailingHeaders().names()) {
                            for (CharSequence value : trailer.trailingHeaders().getAll(name)) {
                                S._debug(logger, log -> log.debug("TRAILING HEADER: " + name + " : " + value));
                                httpRequest.headers().set(name, value);
                                reqWrapper.updateHeaders(headers ->
                                        HttpUtils.appendToMap(headers, name.toString(), value.toString()));
                            }
                        }
                    }

                    //handle the http content TODO add hooks
                    if (contentType == null || contentType.toLowerCase().contains(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toLowerCase())) {

                        String postData = content.toString(CharsetUtil.UTF_8);

                        S._debug(logger, log -> log.debug("postData: " + postData));

                        //default x-www-form-urlencoded parse
                        Map<String, List<String>> postParams = new QueryStringDecoder(postData, CharsetUtil.UTF_8, false).parameters();

                        S._debug(logger, log -> log.debug(S.dump(postParams)));
                        S._for(postParams).each(entry -> {
                            String key = entry.getKey();
                            List<String> value = entry.getValue();
                            S._debug(logger, log -> log.debug(key + S.dump(value)));
                            reqWrapper.updateParams(params -> HttpUtils.appendToMap(params, key, value));
                        });
                    }

                    //build the response
                    NettyRespWrapper respWrapper = new NettyRespWrapper(httpRequest, NettyHttpServer.this);

                    S._debug(logger, log -> log.debug(reqWrapper.toString()));

                    //execution context
                    final ActionCompleteNotification actionCompleteNotification
                            = new ActionCompleteNotification(reqWrapper, respWrapper);
                    respWrapper.acn(actionCompleteNotification);

                    //release httpRequest Ref
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            NettyHttpServer.this.handler().apply(reqWrapper, respWrapper);
                            return actionCompleteNotification;
                        } catch (Throwable th) {
                            actionCompleteNotification.setCause(th);
                            return actionCompleteNotification;
                        }
                    }, executorService).thenAccept(acn -> {
                        try {
                            if (acn.isSuccess()) {
                                switch (acn.type()) {
                                    case ActionCompleteNotification.UNHANDLED: {
                                        logger.warn("unhandled request reach.");
                                        return;
                                    }
                                    case ActionCompleteNotification.NORMAL: {
                                        sendNormal(ctx, acn.response());
                                        return;
                                    }
                                    case ActionCompleteNotification.STATIC_FILE: {
                                        sendFile(ctx, acn.response(), acn.sendfile(),
                                                acn.sendFileOffset(), acn.sendFileLength());
                                    }
                                }
                            } else {
                                //maybe reset, timeout ....
                                ctx.fireExceptionCaught(acn.getCause());
                            }
                        } finally {
                            clean();
                        }
                    });
                }


            }


        }


        Tuple<List<Attribute>, List<FileUpload>> decodeHttpContent(HttpPostRequestDecoder decoder, HttpContent httpContent)
                throws HttpPostRequestDecoder.ErrorDataDecoderException,
                HttpPostRequestDecoder.EndOfDataDecoderException,
                HttpPostRequestDecoder.NotEnoughDataDecoderException {

            decoder.offer(httpContent);

            List<Attribute> attrs = new ArrayList<>();
            List<FileUpload> fileUploads = new ArrayList<>();
            Tuple<List<Attribute>, List<FileUpload>> ret = Tuple.t2(attrs, fileUploads);

            while (decoder.hasNext()) {
                InterfaceHttpData data = decoder.next();
                if (data != null) {
                    // check if current HttpData is a FileUpload and previously set as partial
                    if (partialContent == data) {
                        S._debug(logger, log -> log.debug(" 100% (FinalSize: " + partialContent.length() + ")" + " 100% (FinalSize: " + partialContent.length() + ")"));
                        partialContent = null;
                    }

                    InterfaceHttpData.HttpDataType type = data.getHttpDataType();


                    switch (type) {
                        case Attribute: {
                            Attribute attr = (Attribute) data;

                            S._debug(logger, log -> {
                                try {
                                    log.debug("PARSE ATTR: " + attr.getName() + " : " + attr.getValue());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });

                            attrs.add(attr);
                            break;
                        }
                        case FileUpload: {
                            FileUpload fileUpload = (FileUpload) data;
                            S._debug(logger, log -> {
                                try {
                                    log.debug("PARSE FILE: " + fileUpload.getName()
                                            + " : " + fileUpload.getFilename()
                                            + " : " + fileUpload.getFile().getAbsolutePath());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                            fileUploads.add(fileUpload);
                            break;
                        }
                        //TODO internal attribute
                    }
                }
            }
            return ret;
        }

        void sendFile(ChannelHandlerContext ctx, Response response, RandomAccessFile raf, Long sendoffset, Long sendlength) {
            NettyRespWrapper wrapper = ((NettyRespWrapper) response);

            long offset = sendoffset == null ? 0l : sendoffset;
            long length = 0;
            try {
                length = sendlength == null ? raf.length() : sendlength;
            } catch (IOException e) {
                ctx.fireExceptionCaught(e);
            }

            HttpResponse resp = wrapper.resp;

            ctx.write(resp);
            // Write the content.
            ChannelFuture sendFileFuture;
            ChannelFuture lastContentFuture;
            if (ctx.pipeline().get(SslHandler.class) == null) {
                sendFileFuture =
                        ctx.write(new DefaultFileRegion(raf.getChannel(), offset, length), ctx.newProgressivePromise());
                // Write the end marker.
                lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            } else {
                try {
                    sendFileFuture =
                            ctx.writeAndFlush(new HttpChunkedInput(new ChunkedFile(raf, offset, length, 65536)),
                                    ctx.newProgressivePromise());
                    lastContentFuture = sendFileFuture;

                } catch (IOException e) {
                    ctx.fireExceptionCaught(e);
                    return;
                }
                // HttpChunkedInput will pipe the end marker (LastHttpContent) for us.
            }

            sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
                @Override
                public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
                    if (total < 0) { // total unknown
                        S._debug(BaseServer.logger, logger ->
                                logger.debug(future.channel() + " Transfer progress: " + progress));
                    } else {
                        S._debug(BaseServer.logger, logger ->
                                logger.debug(future.channel() + " Transfer progress: " + progress + " / " + total));
                    }
                }

                @Override
                public void operationComplete(ChannelProgressiveFuture future) throws Exception {
                    S._debug(BaseServer.logger, logger ->
                            logger.debug(future.channel() + " Transfer complete."));
                    S._debug(BaseServer.logger, logger ->
                            logger.debug("all costs: " + (S.now() - wrapper._start_time) + "ms"));
                }
            });

            // Decide whether to close the connection or not.
            if (!HttpHeaderUtil.isKeepAlive(wrapper.request)) {
                // Close the connection when the whole content is written out.
                lastContentFuture.addListener(ChannelFutureListener.CLOSE);
            }
        }

        void sendNormal(ChannelHandlerContext ctx, Response response) {
            NettyRespWrapper wrapper = ((NettyRespWrapper) response);

            HttpResponse resp = wrapper.resp;
            ByteBuf content = wrapper.buffer;

            S._debug(BaseServer.logger, log -> {
                log.debug("----SEND STATUS---");
                log.debug(S.dump(resp.status()));

                log.debug("----SEND HEADERS---");
                log.debug(S.dump(resp.headers()));

                log.debug("----SEND BUFFER DUMP---");
                log.debug(content.toString(CharsetUtil.UTF_8));
            });
            ChannelFuture lastContentFuture;

            //write head
            ctx.write(resp);
            //write content
            ctx.write(content);

            lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            lastContentFuture.addListener(future -> S._debug(BaseServer.logger, logger ->
                    logger.debug("all costs: " + (S.now() - wrapper._start_time) + "ms")));

            if (!HttpHeaderUtil.isKeepAlive(wrapper.request))
                lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }

        void clean() {
            httpRequest = null;
            reqWrapper = null;

            resetDecoder();

            if (content.refCnt() > 0) {
                content.release(content.refCnt());
            }

            content = null;
        }


        void resetDecoder() {
            if (decoder != null) {
                decoder.cleanFiles();
                decoder.destroy();
                decoder = null;
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            clean();

            S._debug(BaseServer.logger, logger -> {
                logger.debug(cause.getMessage());
                cause.printStackTrace();
            });

            if (ctx.channel().isActive()) {
                //send error and close
                ctx.writeAndFlush(new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.INTERNAL_SERVER_ERROR,
                        Unpooled.copiedBuffer(cause.getMessage(), CharsetUtil.UTF_8)
                )).addListener(ChannelFutureListener.CLOSE);
            }

        }

    }


    public void listen() throws Exception {

        //since we only listen on single port
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();

            //max concurrent income connections in queue
            b.option(ChannelOption.SO_BACKLOG, backlog())
                    .option(ChannelOption.SO_REUSEADDR, true);

            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new HttpServerCodec());
                            //pipeline.addLast(new HttpObjectAggregator(65536));
                            //FIXME combine with the chunked writer
                            //pipeline.addLast(new HttpContentCompressor() );
                            pipeline.addLast(new ChunkedWriteHandler());
                            pipeline.addLast(new NettyHttpHandler());
                        }
                    }).childOption(ChannelOption.SO_KEEPALIVE, keepAlive())
            ;

            ChannelFuture f = b.bind(port()).sync();

            f.channel().closeFuture().sync();

        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

}
