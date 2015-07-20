package pond.core.spi.server.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import pond.common.S;
import pond.core.Response;
import pond.core.spi.BaseServer;
import pond.core.spi.server.AbstractServer;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
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
//    private ExecutorService executorService = Executors.newCachedThreadPool();

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

    //util
    @Deprecated
    SslContext buildSslContext() throws Exception {
        if (ssl()) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
        }
        return null;
    }

    class NettyHttpHandler extends SimpleChannelInboundHandler<Object> {

        private HttpData partialContent;

        private HttpPostRequestDecoder decoder;
        //build empty thread vars
        List<ByteBuf> chunks = new ArrayList<>();

        HttpRequest httpRequest = null;

        List<Attribute> attrs = new ArrayList<>();
        List<FileUpload> fileUploads = new ArrayList<>();

        private void sendBadRequest(ChannelHandlerContext ctx) {
            ctx.writeAndFlush(new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.BAD_REQUEST));
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            ctx.flush();
        }


        @Override
        protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof HttpRequest) {
                HttpRequest request = (HttpRequest) msg;

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
                //clean chunks for a new http request
                releaseChunks();
                try {
                    decoder = new HttpPostRequestDecoder(factory, request);
                } catch (HttpPostRequestDecoder.ErrorDataDecoderException err) {
                    logger.error(err.getMessage());
                    S._debug(logger, log -> {
                        err.printStackTrace();
                    });
                    sendBadRequest(ctx);
                    return;
                }
                //readingChunks = HttpHeaderUtil.isTransferEncodingChunked(request);
//                if (readingChunks) {
//                    readingChunks = true;
//                }
            }

            if (msg instanceof HttpContent) {
                HttpContent httpContent = (HttpContent) msg;
                if (!httpContent.decoderResult().isSuccess()) {
                    sendBadRequest(ctx);
                    return;
                }
                ByteBuf chunk;
                if ((chunk = httpContent.content()).isReadable()) {
                    S._debug(BaseServer.logger, log -> {
                        logger.debug("Reading chunk...");
                        logger.debug(S.dump(chunk));
                    });
                    chunks.add(chunk);
                }

                if (decoder != null) {
                    try {
                        decoder.offer(httpContent);
                    } catch (HttpPostRequestDecoder.ErrorDataDecoderException e1) {
                        logger.error(e1.toString());
                        S._debug(logger, log -> e1.printStackTrace());
                        sendBadRequest(ctx);
                        return;
                    }
                    //TODO readHttpDataByChunk

                    try {
                        while (decoder.hasNext()) {
                            InterfaceHttpData data = decoder.next();
                            if (data != null) {
                                // check if current HttpData is a FileUpload and previously set as partial
                                if (partialContent == data) {
                                    S._debug(logger, log -> log.debug(" 100% (FinalSize: " + partialContent.length() + ")" + " 100% (FinalSize: " + partialContent.length() + ")"));
                                    partialContent = null;
                                }
//                                try {
                                // build values
                                processHttpData(data);
//                                } finally {
//                                    data.release();
//                                }
                            }
                        }
                    } catch (HttpPostRequestDecoder.EndOfDataDecoderException e1) {
                        //S.echo("end");
                    }

                    //end of message
                    if (msg instanceof LastHttpContent) {

                        //build the req & resp
                        LastHttpContent trailer = (LastHttpContent) msg;
                        if (!trailer.decoderResult().isSuccess()) {
                            ctx.writeAndFlush(new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                                    HttpResponseStatus.BAD_REQUEST));
                            return;
                        }
                        if (!trailer.trailingHeaders().isEmpty()) {
                            for (CharSequence name : trailer.trailingHeaders().names()) {
                                for (CharSequence value : trailer.trailingHeaders().getAll(name)) {
                                    httpRequest.headers().set(name, value);
                                }
                            }
                        }


                        NettyReqWrapper reqWrapper =
                                new NettyReqWrapper(ctx, httpRequest, NettyHttpServer.this, attrs, fileUploads);

                        reqWrapper.init();

                        NettyRespWrapper respWrapper = new NettyRespWrapper(httpRequest, NettyHttpServer.this);

                        final ActionCompleteNotification actionCompleteNotification =
                                new ActionCompleteNotification(reqWrapper, respWrapper);
                        respWrapper.acn(actionCompleteNotification);


                        //release httpRequest Ref
                        CompletableFuture.supplyAsync(() -> {
                            try {
                                NettyHttpServer.this.handler().apply(reqWrapper, respWrapper);
                            } catch (Throwable th) {
                                actionCompleteNotification.setCause(th);
                            }
                            return actionCompleteNotification;
                        }, ctx.executor()).thenAcceptAsync(acn -> {
                            if (acn.isSuccess()) {
                                switch (acn.type()) {
                                    case ActionCompleteNotification.UNHANDLED: {
                                        clean();
                                        return;
                                    }
                                    case ActionCompleteNotification.NORMAL: {
                                        sendNormal(ctx, acn.response());
                                        return;
                                    }
                                    case ActionCompleteNotification.STATIC_FILE: {
                                        sendFile(ctx, acn.response(), acn.sendfile(),
                                                acn.sendFileOffset(), acn.sendFileOffset());
                                    }
                                }
                            } else {
                                //maybe reset, timeout ....
                                ctx.fireExceptionCaught(acn.getCause());
                            }
                        }, ctx.executor());
                    }
                }

            }


        }

        void sendFile(ChannelHandlerContext ctx, Response response, RandomAccessFile raf, Long sendoffset, Long sendlength) {
            NettyRespWrapper wrapper = ((NettyRespWrapper) response);

            long offset = sendoffset == null ? 0l : sendoffset;
            long length = sendlength == null ? 0l : sendlength;

            HttpResponse resp = wrapper.resp;
            ByteBuf content = wrapper.buffer;

            ctx.write(resp);
//        resp.setStatus(HttpResponseStatus.OK);
//        sendNormal();
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
                    clean();
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
            lastContentFuture.addListener(future -> {
                clean();
                S._debug(BaseServer.logger, logger ->
                        logger.debug("all costs: " + (S.now() - wrapper._start_time) + "ms"));
            });

            if (!HttpHeaderUtil.isKeepAlive(wrapper.request))
                lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }

        void clean() {
            httpRequest = null;
            resetDecoder();
            releaseChunks();
        }

        void processHttpData(InterfaceHttpData data) {

            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                Attribute attr = (Attribute) data;
                attrs.add(attr);
            }
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                FileUpload fileUpload = (FileUpload) data;
                fileUploads.add(fileUpload);
            }
            //TODO internal attribute
        }

        void resetDecoder() {
            if (decoder != null) {
                decoder.cleanFiles();
                decoder.destroy();
                decoder = null;
            }
            attrs.clear();
            fileUploads.clear();
        }

        void releaseChunks() {
            S._for(chunks).each(chunk -> {
                if (chunk.refCnt() > 0)
                    ReferenceCountUtil.release(chunk);
            });
            chunks.clear();

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
                        HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR,
                        Unpooled.copiedBuffer(cause.getMessage(), CharsetUtil.UTF_8
                        )))
                        .addListener(ChannelFutureListener.CLOSE);
            }

        }

    }


    public void listen() throws Exception {

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
                    })

                            //TODO configurations here
                            //TODO interceptors here
                            //TODO fail-back http server here?
                            //TODO baseServer here (discard jetty & the oio or recreate a abstraction FP(req,res) layer?)
                            //TODO
                    .childOption(ChannelOption.SO_KEEPALIVE, keepAlive())
            ;

            ChannelFuture f = b.bind(port()).sync();

            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

}
