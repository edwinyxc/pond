package pond.core.spi.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import pond.common.S;
import pond.core.Response;

import javax.servlet.http.Cookie;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;

public class NettyRespWrapper implements Response {

    ByteBuf buffer;
    NettyOutputStream out;
    PrintWriter writer;

    final NettyHttpServer server;
    final FullHttpRequest request;
    final ChannelHandlerContext ctx;
    final FullHttpResponse resp;

    NettyRespWrapper(ChannelHandlerContext ctx, FullHttpRequest req,
                     NettyHttpServer server) {
        this.server = server;
        this.request = req;
        this.ctx = ctx;
        buffer = Unpooled.buffer();
        out = new NettyOutputStream(buffer);
        writer = new PrintWriter(out);
        resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.ACCEPTED, buffer);

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
        writer.flush();
        S.echo("SENDING CONTENT: ");
        S.echo(buffer.toString(CharsetUtil.US_ASCII));
        S._tap(ctx.writeAndFlush(resp), future -> {

            if (!HttpHeaderUtil.isKeepAlive(request)) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        });
    }

    private void _sendFile() {

    }

    //TODO _send chunked file


    public void sendFile(File f) {

    }

}
