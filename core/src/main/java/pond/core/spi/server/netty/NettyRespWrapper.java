package pond.core.spi.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;
import pond.common.S;
import pond.common.f.Callback;
import pond.core.Response;
import pond.core.http.MimeTypes;
import pond.core.spi.BaseServer;

import javax.servlet.http.Cookie;
import java.io.*;
import java.nio.charset.Charset;

public class NettyRespWrapper implements Response {

    ByteBuf buffer;
    NettyOutputServletStream out;
    PrintWriter writer;

    final NettyHttpServer server;
    final HttpRequest request;
    final ChannelHandlerContext ctx;
    final HttpResponse resp;
    final Callback.C0ERR doClean;

    NettyRespWrapper(ChannelHandlerContext ctx, HttpRequest req,
                     NettyHttpServer server, Callback.C0ERR doClean) {
        this.server = server;
        this.request = req;
        this.ctx = ctx;
        buffer = Unpooled.buffer();
        out = new NettyOutputServletStream(buffer);
        Charset charset = Charset.forName(S.avoidNull(System.getProperty("file.encoding"), "UTF-8"));
        writer = new PrintWriter(new OutputStreamWriter(out, charset));
        resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.ACCEPTED);
        if (HttpHeaderUtil.isKeepAlive(request)) {
            resp.headers().set(HttpHeaderNames.CONNECTION,
                    HttpHeaderValues.KEEP_ALIVE);
        }
        this.doClean = doClean;
    }

    @Override
    public Response header(String k, String v) {
        resp.headers().add(k, v);
        return this;
    }

    @Override
    public void sendError(int code, String msg) {
        write(msg).status(code);
        _send();
    }

    @Override
    public void send(int code, String msg) {
        write(msg).status(code);
        _send();
    }


    private static void setContentTypeHeader(Response response, File file) {
//        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
//        response.contentType(mimeTypesMap.getContentType(file.getName()));
        String filename = file.getName();
        int dot_pos = filename.lastIndexOf(".");
        if (dot_pos != -1 && dot_pos < filename.length() - 1) {
            response.contentType(MimeTypes.getMimeType(filename.substring(dot_pos + 1)));
        } else {
            response.contentType(MimeTypes.MIME_APPLICATION_OCTET_STREAM);
        }
    }

    @Override
    public void sendFile(File file, long offset, long length) {
        resp.setStatus(HttpResponseStatus.OK);
        HttpHeaderUtil.setContentLength(resp, file.length());
        if (resp.headers().get(HttpHeaderNames.CONTENT_TYPE) == null) {
            setContentTypeHeader(this, file);
        }
        ctx.write(resp);
        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException ignored) {
            sendError(404, HttpResponseStatus.NOT_FOUND.toString());
            return;
        }
//        resp.setStatus(HttpResponseStatus.OK);
//        _send();
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
                        ctx.writeAndFlush(new HttpChunkedInput(new ChunkedFile(raf, offset, length, 8192)),
                                ctx.newProgressivePromise());
                lastContentFuture = sendFileFuture;

            } catch (IOException e) {
                S._debug(BaseServer.logger, logger -> {
                    e.printStackTrace();
                    logger.debug(e.getMessage());
                });
                sendError(500, HttpResponseStatus.INTERNAL_SERVER_ERROR + ": " + e.getMessage());
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
                if (doClean != null) {
                    doClean.apply();
                }
            }
        });

        // Decide whether to close the connection or not.
        if (!HttpHeaderUtil.isKeepAlive(request)) {
            // Close the connection when the whole content is written out.
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }

    }

    @Override
    public Response status(int sc) {
        resp.setStatus(HttpResponseStatus.valueOf(sc));
        return this;
    }

    @Override
    public OutputStream out() {
        return out;
    }

    @Override
    public PrintWriter writer() {
        return writer;
    }

    @Override
    public Response write(String s) {
        writer.print(s);
        return this;
    }

    @Override
    public Response cookie(Cookie c) {
        resp.headers().add(HttpHeaderNames.SET_COOKIE,
                ServerCookieEncoder.encode(S._tap(new DefaultCookie(c.getName(), c.getValue()),
                        cookie -> {
                            cookie.setPath(c.getPath());
                            cookie.setSecure(c.getSecure());
                            cookie.setHttpOnly(c.isHttpOnly());
                            cookie.setComment(c.getComment());
                            cookie.setVersion(c.getVersion());
                            cookie.setDomain(c.getDomain());
                        })));
        return this;
    }

    @Override
    public void redirect(String url) {
        resp.headers().add(HttpHeaderNames.LOCATION, url);
        _send();
    }

    @Override
    public Response contentType(String type) {
        resp.headers().add(HttpHeaderNames.CONTENT_TYPE, type);
        return this;
    }

    private void _send() {
        boolean keepAlive;

        writer.flush();

        S._debug(BaseServer.logger, log -> {
            log.debug("----SEND HEADERS---");
            log.debug(S.dump(resp.headers()));

            log.debug("----SEND BUFFER DUMP---");
            log.debug(buffer.toString(CharsetUtil.UTF_8));
        });

        if (keepAlive = HttpHeaderUtil.isKeepAlive(request)) {
            resp.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);

            if (resp.headers().get(HttpHeaderNames.CONTENT_LENGTH) == null) {
                int contentLen = buffer.readableBytes();
                resp.headers().setLong(HttpHeaderNames.CONTENT_LENGTH, contentLen);
            }
        } else {
            resp.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        }


        ChannelFuture  lastContentFuture;

        ctx.write(resp);
        ctx.write(buffer);
        lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        lastContentFuture.addListener(future -> {
            if (doClean != null) {
                doClean.apply();
            }
        });
        if (!keepAlive)
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
//
//        S._tap(ctx.pipe(buffer), then -> {
//            then.addListener(f -> {
//                S._tap(ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT), future -> {
//                    if (!keepAlive) {
//                        future.addListener(ChannelFutureListener.CLOSE);
//                    }
//                });
//            });
//        });

    }


}
