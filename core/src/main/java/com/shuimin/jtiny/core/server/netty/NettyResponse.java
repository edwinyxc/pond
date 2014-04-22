package com.shuimin.jtiny.core.server.netty;

import com.shuimin.jtiny.core.Interrupt;
import com.shuimin.jtiny.core.http.Response;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.ServerCookieEncoder;
import io.netty.handler.stream.ChunkedFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * @author ed
 */
public class NettyResponse implements Response {

    private final FullHttpResponse httpResponse;

    private final ChannelHandlerContext ctx;

    private final OutputStream out;

    private final PrintWriter pw;

    public NettyResponse(
        FullHttpResponse httpResponse,
        ChannelHandlerContext ctx) {
        this.httpResponse = httpResponse;
        this.out = new NettyOutputStream(httpResponse);
        this.pw = new PrintWriter(out);
        this.ctx = ctx;
    }

    @Override
    public Response header(String k, String v) {
        httpResponse.headers().add(k, v);
        return this;
    }

    @Override
    public void send(int code) {
        HttpResponseStatus status = HttpResponseStatus.valueOf(code);
        httpResponse.setStatus(status);
        Interrupt.jump(this);//throw a signal
    }

    @Override
    public void sendError(int code, String msg) {
        HttpResponseStatus status = HttpResponseStatus.valueOf(code);
        httpResponse.setStatus(status);
        writer().print(msg);
        Interrupt.jump(this);//throw a signal

    }

    @Override
    public void sendFile(File file) {
        RandomAccessFile raf ;
        long fileLength ;
        try {
            raf = new RandomAccessFile(file, "r");
        }catch (FileNotFoundException e){
            send(404);
            return;
        }
        try {
            fileLength = raf.length();
            ctx.write(
                new ChunkedFile(raf,0,fileLength,8192), ctx.newProgressivePromise());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Interrupt.jump(this);
    }

    @Override
    public Response status(int sc) {
        httpResponse.setStatus(HttpResponseStatus.valueOf(sc));
        return this;
    }

    @Override
    public Response cookie(Cookie c) {
        httpResponse.headers()
            .add(HttpHeaders.Names.SET_COOKIE,
                ServerCookieEncoder.encode(c.getName(), c.getValue()));
        return this;
    }

    @Override
    public void redirect(String url) {
        httpResponse.setStatus(HttpResponseStatus.MOVED_PERMANENTLY);
        httpResponse.headers().add(HttpHeaders.Names.LOCATION, url);
        Interrupt.jump(this);//throw a signal
    }

    @Override
    public Response contentType(String type) {
        httpResponse.headers().add(HttpHeaders.Names.CONTENT_TYPE, type);
        return this;
    }

    @Override
    public HttpServletResponse raw() {
        //FIXME: 选择之一是加一层HSR
        throw new UnsupportedOperationException("not finished yet.");
    }

    @Override
    public OutputStream out() {
        return out;
    }

    @Override
    public PrintWriter writer() {
        return pw;
    }

    @Override
    public Response write(String s) {
        ctx.write(s);
        return this;
    }

}
