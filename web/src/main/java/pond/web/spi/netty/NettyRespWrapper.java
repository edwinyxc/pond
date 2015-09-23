package pond.web.spi.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import pond.common.S;
import pond.web.Response;
import pond.web.http.Cookie;
import pond.web.http.MimeTypes;
import pond.web.spi.BaseServer;

import java.io.*;
import java.nio.charset.Charset;

public class NettyRespWrapper implements Response {

  ByteBuf buffer;
  OutputStream out;
  PrintWriter writer;

  //  final NettyHttpServer server;
//  final HttpRequest request;
  final HttpResponse resp;
  final HandlerExecutionContext ctx;


  NettyRespWrapper(HandlerExecutionContext ctx) {

    this.ctx = ctx;
    ctx.resp = this;
//    this.request = ctx.req;

    buffer = Unpooled.buffer();
    this.out = new ByteBufOutputStream(buffer);

    Charset charset = Charset.forName(S.avoidNull(System.getProperty("file.encoding"), "UTF-8"));
    writer = new PrintWriter(new OutputStreamWriter(out, charset));

    resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                                   HttpResponseStatus.ACCEPTED);

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
    resp.setStatus(HttpResponseStatus.OK);

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
      //ignored.printStackTrace();
      BaseServer.logger.error(ignored.getMessage(), ignored);
      sendError(404, HttpResponseStatus.NOT_FOUND.toString());
      return;
    }

    ctx.file(raf, offset, length);

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
    resp.headers()
        .add(
            HttpHeaderNames.SET_COOKIE,
            ServerCookieEncoder.encode(S._tap(
                                           new DefaultCookie(c.getName(), c.getValue()),
                                           cookie -> {
                                             cookie.setPath(c.getPath());
                                             cookie.setComment(c.getComment());
                                             cookie.setDomain(c.getDomain());
                                           })
            )
        );
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
    ctx.normal(out);
  }


}
