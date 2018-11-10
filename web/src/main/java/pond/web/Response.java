package pond.web;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.util.CharsetUtil;
import pond.common.f.Callback;
import pond.web.http.CtxHttp;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * <p>Response 封装了http response 对象，
 * 所有Pond支持的服务器都应该提供相应适配器来将底层对象转换成Response</p>
 */
public interface Response {

    /**
     * <p>写入响应头</p>
     *
     * @param k key
     * @param v value
     * @return this
     */
    default Response header(String k, String v) {
        ctx().response().headers().set(k, v);
        return this;
    }

    /**
     * <p>向客户端发送对应代码,内容一并送出
     * 此方法调用之后任何对其的操作都被视为无效，具体细节参考服务期实现。</p>
     *
     * @param code http status code
     */
    default void send(int code) {
        send(200, "");
    }

    /**
     * <p>Send message to client with code 200</p>
     *
     * @param msg
     */
    default void send(String msg) {
        send(200, msg);
    }

    /**
     * <p>此方法用来发送错误码和详细描述</p>
     *
     * @param code
     * @param msg
     */
    default void sendError(int code, String msg) {
        if(code >= 400){
            send(code, msg);
        } else throw new RuntimeException("err code must >= 400");
    }

    /**
     * @param code error code
     * @param msg  error message
     */
    default void send(int code, String msg) {
        ctx().response(HttpResponseStatus.valueOf(code)).write(msg);
    }

    /**
     * <p>向客户端写入文件，完成时发送200 或者　206 (range)，此操作立即返回，具体如何发送由底层服务器控制。</p>
     * big file
     *
     * @param file attachment
     */
    default
    void sendFile(File file, long offset, long length){
        var rebind = (CtxHttp.Send)ctx()::bind;
        try {
            rebind.sendFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    default void sendFile(File file) {
        sendFile(file, 0, file.length());
    }

    /**
     * <p>设置响应状态码，只要不发送，此状态码可以再次改变</p>
     *
     * @param sc status code
     * @return this
     */
    default Response status(int sc) {
        ctx().response(HttpResponseStatus.valueOf(sc));
        return this;
    }

    /**
     * <p>获取底层响应流，具体的底层响应可能不是以流的方式实现的,这可能只是一个模拟。</p>
     *
     * @return outputStream
     */
    default OutputStream out() {
        return ctx().response().outputStream();
    }

    /**
     * <p>包装了out流的printWriter,用于输出响应文本</p>
     *
     * @return printWriter
     */
    @Deprecated
    default PrintWriter writer() {
        return new PrintWriter(out());
    }

    /**
     * <p>向响应添加字符串</p>
     *
     * @param s to append string
     * @return this
     */
    default Response write(String s) {
        ctx().response().write(s);
        return this;
    }

    /**
     * <p>为响应添加cookie, 使用 Add-Cookie 响应头实现</p>
     *
     * @param c
     * @return this
     */
    default Response cookie(Cookie c) {
        ctx().response().headers()
            .add( HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(c));
        return this;
    }

    /**
     * <p>发送302响应，参数作为Location</p>
     *
     * @param url destination location
     */
    default void redirect(String url) {
        var send = (CtxHttp.Send) ctx()::bind;
        var fullResp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.MOVED_PERMANENTLY);
        fullResp.headers().add(HttpHeaderNames.LOCATION, url);
        send.send(fullResp);
    }

    /**
     * <p>设置 Content-Type 响应头
     * </p>
     *
     * @param type type
     * @return this
     */
    default Response contentType(String type){
        ctx().response().headers().set(HttpHeaderNames.CONTENT_TYPE, type);
        return this;
    }

    @Deprecated
    default void render(Render r) {
        r.apply(ctx());
    }

//  default <T> void render(Callback.C2<Context,T> r, T t) {
//      r.apply(ctx(), t);
//  }

    CtxHttp ctx();


}
