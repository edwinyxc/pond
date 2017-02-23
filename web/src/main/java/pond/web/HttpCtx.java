package pond.web;

import io.netty.buffer.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import pond.common.S;
import pond.common.f.Callback;
import pond.web.http.Cookie;
import pond.web.http.MimeTypes;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Execution Context, attached to a single thread.
 */
public class HttpCtx extends Ctx {
  final static int SEND_UNHANDLED = 0;
  final static int SEND_NORMAL = 1;
  final static int SEND_STATIC_FILE = 2;
  final static int SEND_ERROR = -1;
  final static int SEND_UPGRADE_TO_WEBSOCKET = 3;

  RandomAccessFile sendfile;
  Long sendfile_offset;
  Long sendfile_length;
  int send_type = SEND_UNHANDLED;

  public OutputStream out;
  public PrintWriter printWriter;
  Throwable cause;

  public final Charset charset = Charset.forName(
      S.avoidNull(System.getProperty("file.encoding"),
                  "UTF-8")
  );


  final HttpRequest nettyRequest;
  HttpResponse nettyResponse = new DefaultHttpResponse(
      HttpVersion.HTTP_1_1,
      HttpResponseStatus.ACCEPTED
  );
  final boolean isMultipart;
  final boolean isKeepAlive;
  final CompositeByteBuf inboundByteBuf;
  final ByteBuf outBoundByteBuf = Unpooled.buffer();

  final protected Map<String, List<String>> headers = new HashMap<>();
  final protected Map<String, List<String>> params = new HashMap<>();
  final protected Map<String, List<Request.UploadFile>> uploadFiles = new HashMap<>();
  final protected Map<String, Cookie> cookies = new HashMap<>();


  HttpCtx flagToSendNormal() {
    this.send_type = SEND_NORMAL;
    this.out = S.avoidNull(
        this.out,
        new ByteBufOutputStream(outBoundByteBuf)
    );
    this.printWriter = S.avoidNull(
        this.printWriter,
        new PrintWriter(new OutputStreamWriter(out, charset))
    );
    return this;
  }

  HttpCtx flagToSendFile(RandomAccessFile file,
                         Long offset,
                         Long length) {
    sendfile = file;
    sendfile_offset = offset;
    sendfile_length = length;
    this.send_type = SEND_STATIC_FILE;
    return this;
  }

  HttpCtx updateParams(Callback<Map<String, List<String>>> b) {
    b.apply(params);
    return this;
  }

  HttpCtx updateHeaders(Callback<Map<String, List<String>>> b) {
    b.apply(headers);
    return this;
  }

  HttpCtx updateUploadFiles(Callback<Map<String, List<Request.UploadFile>>> b) {
    b.apply(uploadFiles);
    return this;
  }

  HttpCtx updateCookies(Callback<Map<String, Cookie>> b) {
    b.apply(cookies);
    return this;
  }

  public final Request req = new Request() {

    @Override
    public String method() {
      return HttpCtx.this.method;
    }

    @Override
    public String remoteIp() {
      return HttpCtx.this.context.channel().remoteAddress().toString();
    }

    @Override
    public InputStream in() {
      return new ByteBufInputStream(inboundByteBuf);
    }

    @Override
    public String uri() {
      return HttpCtx.this.uri;
    }

    @Override
    public Map<String, List<String>> headers() {
      return headers;
    }

    @Override
    public Map<String, List<String>> params() {
      Map<String, List<String>> all_params = new HashMap<>();
      all_params.putAll(params);
      all_params.putAll(inUrlParams);
      return all_params;
    }

    @Override
    public Map<String, List<Request.UploadFile>> files() {
      return uploadFiles;
    }

    @Override
    public Map<String, Cookie> cookies() {
      return cookies;
    }

    @Override
    public String path() {
      return path;
    }

    @Override
    public HttpCtx ctx() {
      return HttpCtx.this;
    }
  };

  public final Response resp = new Response() {


    @Override
    public Response header(String k, String v) {
      nettyResponse.headers().add(k, v);
      return this;
    }

    @Override
    public void sendError(int code, String msg) {
      flagToSendNormal();
      setHandled();
      write(msg).status(code);
    }

    @Override
    public void send(int code, String msg) {
      flagToSendNormal();
      setHandled();
      write(msg).status(code);
    }

    @Override
    public void sendFile(File file, long offset, long length) {
      nettyResponse.setStatus(HttpResponseStatus.OK);

      HttpHeaderUtil.setContentLength(nettyResponse, file.length());

      if (nettyResponse.headers().get(HttpHeaderNames.CONTENT_TYPE) == null) {
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
      flagToSendFile(raf, offset, length);
      setHandled();
    }

    @Override
    public Response status(int sc) {
      nettyResponse.setStatus(HttpResponseStatus.valueOf(sc));
      return this;
    }

    @Override
    public OutputStream out() {
      flagToSendNormal();
      return out;
    }

    @Override
    public PrintWriter writer() {
      flagToSendNormal();
      return printWriter;
    }

    @Override
    public Response write(String s) {
      flagToSendNormal();
      printWriter.print(s);
      return this;
    }

    @Override
    public Response cookie(Cookie c) {
      nettyResponse.headers()
          .add(
              HttpHeaderNames.SET_COOKIE,
              ServerCookieEncoder.encode(
                  S._tap(
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
      nettyResponse.setStatus(HttpResponseStatus.MOVED_PERMANENTLY);
      nettyResponse.headers().add(HttpHeaderNames.LOCATION, url);
    }

    @Override
    public Response contentType(String type) {
      nettyResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, type);
      return this;
    }

    @Override
    public HttpCtx ctx() {
      return HttpCtx.this;
    }

  };

  public HttpCtx(HttpRequest nettyRequest,
                 ChannelHandlerContext context,
                 Boolean isKeepAlive,
                 Boolean isMultipart,
                 CompositeByteBuf inboundByteBuf) {
    super(nettyRequest.method().toString(), nettyRequest.uri(), nettyRequest, context);
    this.nettyRequest = nettyRequest;
//    this.method = nettyRequest.method().toString();
    this.isKeepAlive = isKeepAlive;
    this.isMultipart = isMultipart;
    this.inboundByteBuf = inboundByteBuf;
  }

}
