package pond.web;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;
import pond.common.S;
import pond.common.STRING;
import pond.common.f.Callback;
import pond.common.f.Tuple;
import pond.web.http.Cookie;
import pond.web.http.HttpUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * Created by ed on 9/3/15.
 */
class NettyHttpHandler extends SimpleChannelInboundHandler<Object> {

    private Ctx webCtx;
    final CtxHandler handler;
    final static String WEBSOCKET_HANDLER_LABEL = "WEBSOCKET_HANDLER_LABEL";

    static {
        DiskFileUpload.deleteOnExitTemporaryFile = true; // should delete file
        // on exit (in normal // exit)
        DiskFileUpload.baseDirectory = null; // system temp directory
        DiskAttribute.deleteOnExitTemporaryFile = true; // should delete file on
        // exit (in normal exit)
        DiskAttribute.baseDirectory = null; // system temp directory
    }

    private HttpPostRequestDecoder decoder;
    //hold the ctx;
//  final Map<ChannelHandlerContext, HttpCtx> ctxRegister = new LinkedHashMap<>()LinkedHashMap;
    //use for websocket
//  final static Map<Channel, HttpCtx> channelRegister = new ConcurrentHashMap<>();
//  final ExecutorService executorService;

    NettyHttpHandler(CtxHandler handler ) {
        this.handler = handler;
    }

    private static final HttpDataFactory factory =
            new DefaultHttpDataFactory(DefaultHttpDataFactory.MAXSIZE); // Disk if size exceed

    private void sendBadRequest(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.BAD_REQUEST))
                .addListener(ChannelFutureListener.CLOSE);
    }

//  private PreprocessedWebCtx preprocessedWebCtx = null;

    private HttpCtx receiveHttpRequest(ChannelHandlerContext ctx,
                                       HttpRequest request) {

        if (HttpHeaderUtil.is100ContinueExpected(request)) {
            ctx.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.CONTINUE));
        }

        //build the req

        boolean isKeepAlive = HttpHeaderUtil.isKeepAlive(request);
        boolean isMultipart = HttpPostRequestDecoder.isMultipart(request);
        CompositeByteBuf content = null;

        S._debug(BaseServer.logger, log -> {
            log.debug("GOT HTTP REQUEST:");
            log.debug(request.toString());
        });

        if (!request.decoderResult().isSuccess()) {
            sendBadRequest(ctx);
        }

//    contentType = request.headers().getAndConvert(HttpHeaderNames.CONTENT_TYPE);
        //build the multipart decoder
        if (isMultipart) {
            HttpMethod method = request.method();

            if (method.equals(HttpMethod.POST)
                    || method.equals(HttpMethod.PUT)
                    || method.equals(HttpMethod.PATCH)) {
                try {
                    decoder = new HttpPostRequestDecoder(factory, request);
                } catch (HttpPostRequestDecoder.ErrorDataDecoderException err) {
                    BaseServer.logger.error(err.getMessage(), err);
                    sendBadRequest(ctx);
                }
            } else {
                BaseServer.logger.error("unexpected multipart request caught : invalid http method: " + method);
                sendBadRequest(ctx);
            }
        } else {
            //manual handle
            content = Unpooled.compositeBuffer();
        }

        HttpCtx httpCtx = new HttpCtx(request, ctx, isKeepAlive, isMultipart, content);

        //do update
        //parse headers
        final HttpRequest finalHttpRequest = request;
        //uri-queries
        Map<String, List<String>>
                parsedParams = new QueryStringDecoder(request.uri()).parameters();

        String _uri = request.uri();

        S._debug(BaseServer.logger, log -> {
            log.debug("QUERY STRING: " + _uri);
            log.debug("PARSED PARAMS: " + S.dump(parsedParams));
        });
        httpCtx.updateHeaders(
                headers ->
                        S._for(finalHttpRequest.headers())
                                .each(entry ->
                                        HttpUtils.appendToMap(
                                                headers,
                                                entry.getKey().toString(),
                                                finalHttpRequest.headers()
                                                        .getAllAndConvert(entry.getKey())
                                        )
                                )
        );
        httpCtx.updateParams(params -> params.putAll(parsedParams));

        //parse cookies
        httpCtx.updateCookies(cookies -> {

            Set<io.netty.handler.codec.http.Cookie>
                    decoded = ServerCookieDecoder.decode(
                    S.avoidNull(
                            request.headers().getAndConvert(HttpHeaderNames.COOKIE)
                            , "")
            );

            S._for(decoded).each(
                    cookie ->
                            cookies.put(cookie.name(), S._tap(
                                    new Cookie(cookie.name(), cookie.value()),
                                    c -> {
                                        c.setPath(cookie.path());
                                        c.setMaxAge((int) cookie.maxAge());
                                        if (STRING.notBlank(cookie.domain())) {
                                            c.setDomain(cookie.domain());
                                        }
                                        c.setComment(cookie.comment());
                                    }))
            );

        });

        return httpCtx;
    }

    private void receiveHttpContent(ChannelHandlerContext ctx,
                                    HttpContent httpContent,
                                    HttpCtx preprocessed) {

        if (!httpContent.decoderResult().isSuccess()) {
            sendBadRequest(ctx);
            return;
        }

        boolean isMultipart = preprocessed.isMultipart;
        boolean isKeepAlive = preprocessed.isKeepAlive;
        HttpRequest request = preprocessed.nettyRequest;
        CompositeByteBuf pooledBuffer = preprocessed.inboundByteBuf;

        //multipart
        if (decoder != null && isMultipart) {
            try {
                Tuple<List<Attribute>, List<FileUpload>>
                        tuple = decodeHttpContent(decoder, httpContent);

                preprocessed.updateUploadFiles(
                        files -> S._for(tuple._b).each(
                                fileUpload -> HttpUtils.appendToMap(
                                        files, fileUpload.getName(), new NettyUploadFile(fileUpload))
                        )
                );

                preprocessed.updateParams(
                        params ->
                                S._for(tuple._a).each(attr -> {
                                    String k = attr.getName();
                                    String v = S._try_ret(attr::getValue);
                                    S._debug(BaseServer.logger, log -> log.debug(k + " " + S.dump(v)));
                                    HttpUtils.appendToMap(params, k, v);
                                })
                );

            } catch (HttpPostRequestDecoder.EndOfDataDecoderException endo) {
                //fine & it`s normal & OK  & do nothing
                S._debug(BaseServer.logger, logger -> {
                    logger.debug("EndOfDataDecoder");
                });

            } catch (Exception e1) {
                BaseServer.logger.error(e1.getMessage(), e1);
                S._debug(BaseServer.logger, log -> log.debug(e1.getMessage(), e1));
                sendBadRequest(ctx);
                return;
            }

        } else {
            //merge chunks
            ByteBuf chunk = httpContent.content();
            //in this case, the pooledBuffer must has been initialized.
            S._assert(pooledBuffer);
            if (chunk.isReadable()) {
                chunk.retain();
                pooledBuffer.addComponent(httpContent.content());
                pooledBuffer.writerIndex(pooledBuffer.writerIndex() + chunk.readableBytes());
            }
        }

        //end of message
        if (httpContent instanceof LastHttpContent) {
//      S._assert(preprocessed);
            //bind inputStream
//      if (pooledBuffer != null) {
//        .content(pooledBuffer);
//      }

            //merge trailing headers
            LastHttpContent trailer = (LastHttpContent) httpContent;
            if (!trailer.decoderResult().isSuccess()) {
                ctx.writeAndFlush(
                        new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                                HttpResponseStatus.BAD_REQUEST)
                );
                return;
            }

            //trailing headers
            if (!trailer.trailingHeaders().isEmpty()) {
                for (CharSequence name : trailer.trailingHeaders().names()) {
                    for (CharSequence value : trailer.trailingHeaders().getAll(name)) {
                        S._debug(BaseServer.logger,
                                log -> log.debug("TRAILING HEADER: " + name + " : " + value));
                        request.headers().set(name, value);
//            S._assert(reqWrapper);
                        preprocessed.updateHeaders(
                                headers -> HttpUtils.appendToMap(headers, name.toString(), value.toString()));
                    }
                }
            }

            String contentType = null;
            String postData;
            if (request != null) {
                contentType = request.headers().getAndConvert(HttpHeaderNames.CONTENT_TYPE);
            }
            //handle the http content TODO add hooks
            //www
            if ((contentType == null
                    || contentType.toLowerCase().contains(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toLowerCase()))
                    && pooledBuffer != null
                    && STRING.notBlank(postData = pooledBuffer.toString(CharsetUtil.UTF_8))) {
                final String finalPostData = postData;
                S._debug(BaseServer.logger, log -> log.debug("postData: " + finalPostData));

                //default x-www-form-urlencoded parse
                Map<String, List<String>> postParams = new QueryStringDecoder(postData, CharsetUtil.UTF_8, false).parameters();

                S._debug(BaseServer.logger, log -> log.debug(S.dump(postParams)));
                S._for(postParams).each(entry -> {
                    String key = entry.getKey();
                    List<String> value = entry.getValue();
                    S._debug(BaseServer.logger, log -> log.debug(key + S.dump(value)));
                    S._assert(preprocessed);
                    preprocessed.updateParams(params -> HttpUtils.appendToMap(params, key, value));
                });
            }
      /*else if ((contentType == null
          || contentType.toLowerCase().contains("json"))
          && pooledBuffer != null
          && STRING.notBlank(postData = pooledBuffer.toString(CharsetUtil.UTF_8))) {
        //TODO add xml or json parser
//        final String finalPostData = postData;
//        S._debug(BaseServer.logger, log -> log.debug("postData: " + finalPostData));
//
//        Map parseJson = JSON.parse(finalPostData);
//
//        S._debug(BaseServer.logger, log -> log.debug(S.dump(postParams)));
//        S._for(postParams).each(entry -> {
//          String key = entry.getKey();
//          List<String> value = entry.getValue();
//          S._debug(BaseServer.logger, log -> log.debug(key + S.dump(value)));
//          reqWrapper.updateParams(params -> HttpUtils.appendToMap(params, key, value));
//        });
      }*/
            else {
                //TODO REFACTOR customized content parser
                //json
            }

            //execution context

            S._debug(BaseServer.logger, log -> log.debug(String.valueOf(preprocessed)));

            long _start_time = S.now();

            S._debug(BaseServer.logger, logger -> {
                logger.debug("resp build at " + _start_time);
            });

            S._debug(BaseServer.logger,
                    log -> log.debug("TRACE: run the exe-ctx"));

            try {
                handler.apply(preprocessed);
                S._assert(preprocessed.context == ctx);
                S._debug(BaseServer.logger,
                        log -> log.debug("exe-ctx costs " + (S.now() - _start_time) + " ms"));
                if (preprocessed.send_type != HttpCtx.SEND_ERROR) {
                    switch (preprocessed.send_type) {
                        case HttpCtx.SEND_UPGRADE_TO_WEBSOCKET: {
                            BaseServer.logger.info("upgrade to websocket");
                            //Upgrade httpCtx to WSCtx
                            // Handshake
                            WebSocketServerHandshakerFactory wsFactory =
                                    new WebSocketServerHandshakerFactory(
                                            "ws://"
                                                    + preprocessed.nettyRequest.headers().get(HttpHeaderNames.HOST)
                                                    + preprocessed.path,
                                            null,
                                            false);

                            WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(preprocessed.nettyRequest);
                            if (handshaker == null) {
                                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(preprocessed.context.channel());
                            } else {
                                //build DefaultFullHttpRequest
                                HttpRequest req = preprocessed.nettyRequest;
                                FullHttpRequest fullreq =
                                        new DefaultFullHttpRequest(request.protocolVersion(),
                                                request.method(),
                                                request.uri(),
                                                preprocessed.inboundByteBuf,
                                                true);
                                S._for(request.headers().entries()).each(l -> {
                                    fullreq.headers().add(l.getKey(), l.getValue());
                                });

                                handshaker.handshake(preprocessed.context.channel(), fullreq);
                                //get the callback
                                Callback<WSCtx> cb =
                                        (Callback<WSCtx>) preprocessed.get(NettyHttpHandler.WEBSOCKET_HANDLER_LABEL);
                                //build new Context
                                WSCtx wsCtx = new WSCtx(preprocessed, handshaker);
                                this.webCtx = wsCtx;
                                cb.apply(wsCtx);
                                //fire open event
//                                wsCtx.onOpenHandler.apply(wsCtx);
                                //close this http request
                                //set upgrade flag
                                //TODO websocketContext bind here
                                return;
                            }
                        }
                        case HttpCtx.SEND_UNHANDLED: {
                            BaseServer.logger.warn("unhandled request reach. send 400 bad request");
                            sendBadRequest(ctx);
                            return;
                        }
                        case HttpCtx.SEND_NORMAL: {
                            sendNormal(preprocessed);
                            return;
                        }
                        case HttpCtx.SEND_STATIC_FILE: {
                            sendFile(ctx,
                                    preprocessed.nettyResponse,
                                    preprocessed.sendfile,
                                    preprocessed.sendfile_offset,
                                    preprocessed.sendfile_length
                            );
                        }
                    }
                } else {
                    //may reset, timeout ....
                    //ctx.fireExceptionCaught(exe_ctx.getCause());
                }
            } catch (Exception e) {
                ctx.fireExceptionCaught(e);
            } finally {
                S._debug(BaseServer.logger,
                        log -> log.debug("TRACE: IO-SEND finished, now make clean"));
                clean(ctx);
                S._debug(BaseServer.logger,
                        log -> log.debug("TRACE: Clean finished"));
            }
        }

        S._debug(BaseServer.logger,
                log -> log.debug("TRACE: IO-READ finished"));

    }


    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) {
        //create webCtx
        //declare the in-request-scope-refs
        Ctx preprocessed;
        if (msg instanceof HttpRequest) {
            preprocessed = receiveHttpRequest(ctx, (HttpRequest) msg);
            webCtx = preprocessed;
        } else if (msg instanceof HttpContent) {
            Ctx found = webCtx;
            if (null == found) {
                ctx.fireExceptionCaught(
                        new RuntimeException("FOUND CONTENT WITHOUT REQUEST" + ((HttpContent) msg).content().toString(CharsetUtil.UTF_8)));
            } else receiveHttpContent(ctx, (HttpContent) msg, ((HttpCtx) found));
        } else if (msg instanceof WebSocketFrame) {
            //TODO WSCTX
            WebSocketFrame frame = (WebSocketFrame) msg;
            // get wsctx;
            WSCtx wsCtx = (WSCtx) this.webCtx;
            //build wsctx

            S._assert(wsCtx);
            // Check for closing frame
            if (frame instanceof CloseWebSocketFrame) {
                wsCtx.onCloseHandler.apply(wsCtx);
                wsCtx.handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
                return;
            }
            if (frame instanceof PingWebSocketFrame) {
                ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
                return;
            }
            if (!(frame instanceof TextWebSocketFrame)) {
                throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass()
                        .getName()));
            }

            // Send the uppercase string back.
            String request = ((TextWebSocketFrame) frame).text();
            wsCtx.onMessageHandler.apply(request, wsCtx);
        } else {
            //bad request
            System.out.println(S.dump(msg));
            ctx.fireExceptionCaught(new NullPointerException("bad request"));
        }
    }


    Tuple<List<Attribute>, List<FileUpload>> decodeHttpContent
            (HttpPostRequestDecoder decoder, HttpContent httpContent)
            throws HttpPostRequestDecoder.ErrorDataDecoderException,
            HttpPostRequestDecoder.EndOfDataDecoderException,
            HttpPostRequestDecoder.NotEnoughDataDecoderException {

        decoder.offer(httpContent);

        List<Attribute> attrs = new ArrayList<>();
        List<FileUpload> fileUploads = new ArrayList<>();
        Tuple<List<Attribute>, List<FileUpload>> ret = Tuple.pair(attrs, fileUploads);

        while (decoder.hasNext()) {
            InterfaceHttpData data = decoder.next();
            if (data != null) {
//          // check if current HttpData is a FileUpload and previously set as partial
//          if (partialContent == data) {
//            S._debug(logger, log -> log.debug(" 100% (FinalSize: " + partialContent.length() + ")" + " 100% (FinalSize: " + partialContent.length() + ")"));
////            partialContent = null;
//          }

                InterfaceHttpData.HttpDataType type = data.getHttpDataType();


                switch (type) {
                    case Attribute: {
                        Attribute attr = (Attribute) data;

                        S._debug(BaseServer.logger, log -> {
                            try {
                                log.debug("PARSE ATTR: " + attr.getName() + " : " + attr.getValue());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });

                        attrs.add(attr);
                        break;
                    }
                    case FileUpload: {
                        FileUpload fileUpload = (FileUpload) data;
                        S._debug(BaseServer.logger, log -> {
                            try {
                                log.debug("PARSE FILE: " + fileUpload.getName()
                                        + " : " + fileUpload.getFilename()
                                        + " : " + fileUpload.getFile().getAbsolutePath());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
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

    void sendFile(ChannelHandlerContext ctx,
                  HttpResponse resp,
                  RandomAccessFile raf,
                  Long sendoffset,
                  Long sendlength) {

        long offset = sendoffset == null ? 0l : sendoffset;
        long length = 0;
        try {
            length = sendlength == null ? raf.length() : sendlength;
        } catch (IOException e) {
            ctx.fireExceptionCaught(e);
        }

        ctx.write(resp);
        // Write the content.
        ChannelFuture sendFileFuture;
//    if (ctx.pipeline().get(SslHandler.class) == null) {
//      sendFileFuture =
//          ctx.write(new DefaultFileRegion(raf.getChannel(), offset, length), ctx.newProgressivePromise());
//      // Write the end marker.
//      lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
//    } else {
        try {
            sendFileFuture = ctx.write(new HttpChunkedInput(new ChunkedFile(raf, offset, length, 65536)),
                    ctx.newProgressivePromise());

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
                }
            });

            writeLastContentAndFlush(ctx);

        } catch (IOException e) {
            ctx.fireExceptionCaught(e);
        }
        // HttpChunkedInput will pipe the end marker (LastHttpContent) for us.
//    }


//    // Decide whether to close the connection or not.
//    if (!isKeepAlive)
//      // Close the connection when the whole content is written out.
//      lastContentFuture.addListener(ChannelFutureListener.CLOSE);

    }

    void sendNormal(HttpCtx httpCtx) {

        httpCtx.resp.writer().flush();
        HttpResponse response = httpCtx.nettyResponse;
        //sendNormal
        if (httpCtx.isKeepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION,
                    HttpHeaderValues.KEEP_ALIVE);

            if (response.headers().get(HttpHeaderNames.CONTENT_LENGTH) == null) {
                int contentLen = httpCtx.outBoundByteBuf.readableBytes();
                response.headers().setLong(HttpHeaderNames.CONTENT_LENGTH, contentLen);
            }
        } else {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        }


        ByteBuf content = httpCtx.outBoundByteBuf;

        S._debug(BaseServer.logger, log -> {
            log.debug("----SEND STATUS---");
            log.debug(S.dump(response.status()));

            log.debug("----SEND HEADERS---");
            log.debug(S.dump(response.headers()));

            log.debug("----SEND BUFFER DUMP---");

            if (content.readableBytes() > 1000)
                log.debug("Content too large to display!");
            else
                log.debug(content.toString(CharsetUtil.UTF_8));
        });

        //write head
        httpCtx.context.write(response);
        //write content
        httpCtx.context.write(content);
        content.release();
        writeLastContentAndFlush(httpCtx.context);
    }

    void writeLastContentAndFlush(ChannelHandlerContext ctx) {

        HttpCtx preprocessedWebCtx = (HttpCtx) webCtx;
        S._assert(preprocessedWebCtx);
        if (ctx.executor().inEventLoop()) {
            ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            if (!preprocessedWebCtx.isKeepAlive)
                future.addListener(ChannelFutureListener.CLOSE);
        } else {
            ctx.executor().execute(() -> {
                ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
                if (!preprocessedWebCtx.isKeepAlive)
                    future.addListener(ChannelFutureListener.CLOSE);
            });
        }
    }


    void clean(ChannelHandlerContext ctx) {

        HttpCtx preprocessedWebCtx = (HttpCtx) webCtx;
        if (preprocessedWebCtx != null) {
            ByteBuf byteBuf = preprocessedWebCtx.inboundByteBuf;
            if (byteBuf != null && byteBuf.refCnt() > 0) {
                byteBuf.release(byteBuf.refCnt());
            }
            S._debug(BaseServer.logger, log -> log.debug("RELEASING IO-CTX: " + ctx.toString()));
            resetDecoder();
        }
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

        S._debug(BaseServer.logger, logger -> {
            if (cause != null) logger.debug(cause.getMessage(), cause);
            else logger.debug("UNKNOWN ERROR CAUGHT");
        });

        clean(ctx);

        if (ctx.channel().isActive()) {
            //send error and close
            ctx.writeAndFlush(new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.BAD_REQUEST,
                    Unpooled.copiedBuffer(cause.getMessage(), CharsetUtil.UTF_8)
            )).addListener(ChannelFutureListener.CLOSE);
        }

    }

}

