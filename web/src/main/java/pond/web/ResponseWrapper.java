package pond.web;

import pond.web.http.Cookie;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;

public class ResponseWrapper implements Response {

  final Response wrapped;
  final Ctx ctx;

  ResponseWrapper(Ctx ctx, Response w) {
    wrapped = w;
    this.ctx = ctx;
  }

  @Override
  public Ctx ctx() {
    return ctx;
  }

  void setSendFlag() {
    ctx().setHandled(true);
  }

  @Override
  public Response header(String k, String v) {
    return wrapped.header(k, v);
  }

  @Override
  public void sendError(int code, String msg) {
    wrapped.sendError(code, msg);
    setSendFlag();
  }

  @Override
  public void send(int code, String msg) {
    wrapped.send(code, msg);
    setSendFlag();
  }

  @Override
  public void sendFile(File file, long offset, long length) {
    wrapped.sendFile(file, offset, length);
    setSendFlag();
  }


  @Override
  public Response status(int sc) {
    return wrapped.status(sc);
  }

  @Override
  public OutputStream out() {
    return wrapped.out();
  }

  @Override
  public PrintWriter writer() {
    return wrapped.writer();
  }

  @Override
  public Response write(String s) {
    return wrapped.write(s);
  }

  @Override
  public Response cookie(Cookie c) {
    return wrapped.cookie(c);
  }

  @Override
  public void redirect(String url) {
    wrapped.redirect(url);
    setSendFlag();
  }

  @Override
  public Response contentType(String type) {
    return wrapped.contentType(type);
  }
}
