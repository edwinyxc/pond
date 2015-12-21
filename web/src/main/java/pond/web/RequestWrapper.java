package pond.web;

import pond.web.http.Cookie;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by ed on 12/2/15.
 */
public class RequestWrapper implements Request{

  final Request wrapped;
  final WebCtx ctx;

  public RequestWrapper(WebCtx ctx, Request wrapped) {
    this.wrapped = wrapped;
    this.ctx = ctx;
  }

  @Override
  public String method() {
    return wrapped.method();
  }

  @Override
  public String remoteIp() {
    return wrapped.method();
  }

  @Override
  public InputStream in() {
    return wrapped.in();
  }

  @Override
  public String uri() {
    return wrapped.uri();
  }

  @Override
  public Map<String, List<String>> headers() {
    return wrapped.headers();
  }

  @Override
  public Map<String, List<String>> params() {
    return wrapped.params();
  }

  @Override
  public Map<String, List<UploadFile>> files() {
    return wrapped.files();
  }

  @Override
  public Map<String, Cookie> cookies() {
    return wrapped.cookies();
  }

  @Override
  public WebCtx ctx() {
    return ctx;
  }
}
