package com.shuimin.pond.core.spi.server.netty;

import com.shuimin.pond.core.http.Response;
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

    private boolean hasSend = false;

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
        if(hasSend) return;
        HttpResponseStatus status = HttpResponseStatus.valueOf(code);
        httpResponse.setStatus(status);
        try {
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        hasSend = true;
    }

    @Override
    public void sendError(int code, String msg) {
        if(hasSend) return;
        HttpResponseStatus status = HttpResponseStatus.valueOf(code);
        httpResponse.setStatus(status);
        writer().print(msg);
        hasSend = true;
        try {
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendFile(File file) {
        if(hasSend) return ;
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
        hasSend = true;
        try {
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        try {
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
