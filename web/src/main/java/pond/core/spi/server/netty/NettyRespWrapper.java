package pond.core.spi.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import pond.common.S;
import pond.core.Response;
import pond.core.http.Cookie;
import pond.core.http.MimeTypes;
import pond.core.spi.BaseServer;

import java.io.*;
import java.nio.charset.Charset;

public class NettyRespWrapper implements Response {

    ByteBuf buffer;
    OutputStream out;
    PrintWriter writer;
    long _start_time;

    final NettyHttpServer server;
    final HttpRequest request;
    final HttpResponse resp;
    ActionCompleteNotification acn;


    NettyRespWrapper(HttpRequest req, NettyHttpServer server) {
        this.server = server;
        this.request = req;

        buffer = Unpooled.buffer();
        this.out = new ByteBufOutputStream(buffer);

        Charset charset = Charset.forName(S.avoidNull(System.getProperty("file.encoding"), "UTF-8"));
        writer = new PrintWriter(new OutputStreamWriter(out, charset));

        resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.ACCEPTED);

        S._debug(BaseServer.logger, logger -> {
            this._start_time = S.now();
            logger.debug("resp build at " + _start_time);
        });
    }

    public void acn(ActionCompleteNotification acn) {
        this.acn = acn;
    }

    @Override
    public Response header(String k, String v) {
        resp.headers().add(k, v);
        return this;
    }

    @Override
    public void sendError(int code, String msg) {
        write(msg).status(code);
        sendNormal();
    }

    @Override
    public void send(int code, String msg) {
        write(msg).status(code);
        sendNormal();
    }


    @Override
    public void sendFile(File file, long offset, long length) {
        S._debug(BaseServer.logger, logger ->
                logger.debug("user porcess costs: " + (S.now() - _start_time) + "ms"));
        resp.setStatus(HttpResponseStatus.OK);

        if (HttpHeaderUtil.isKeepAlive(request)) HttpHeaderUtil.setKeepAlive(resp, true);

        HttpHeaderUtil.setContentLength(resp, file.length());

        if (resp.headers().get(HttpHeaderNames.CONTENT_TYPE) == null) {
            String filename = file.getName();
            int dot_pos = filename.lastIndexOf(".");
            if (dot_pos != -1 && dot_pos < filename.length() - 1) {
                this.contentType(MimeTypes.getMimeType(filename.substring(dot_pos + 1)));
            } else {
                this.contentType(MimeTypes.MIME_APPLICATION_OCTET_STREAM);
            }
        }


        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException ignored) {
            //TODO
            ignored.printStackTrace();
            sendError(404, HttpResponseStatus.NOT_FOUND.toString());
            return;
        }

        acn.file(raf, offset, length);

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
        resp.setStatus(HttpResponseStatus.MOVED_PERMANENTLY);
        resp.headers().add(HttpHeaderNames.LOCATION, url);
        sendNormal();
    }

    @Override
    public Response contentType(String type) {
        resp.headers().add(HttpHeaderNames.CONTENT_TYPE, type);
        return this;
    }

    private void sendNormal() {

        S._debug(BaseServer.logger, logger ->
                logger.debug("user porcess costs: " + (S.now() - _start_time) + "ms"));
        writer.flush();

        //sendNormal

        boolean keepAlive;


        if (keepAlive = HttpHeaderUtil.isKeepAlive(request)) {

            resp.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);

            if (resp.headers().get(HttpHeaderNames.CONTENT_LENGTH) == null) {
                int contentLen = buffer.readableBytes();
                resp.headers().setLong(HttpHeaderNames.CONTENT_LENGTH, contentLen);
            }
        } else {
            resp.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        }

        //set acn to normal state
        acn.normal(out);


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
