package pond.web;

import pond.common.*;
import pond.core.CtxHandler;
import pond.web.http.HttpCtx;
import pond.web.http.MimeTypes;

import java.io.*;
import java.net.URLEncoder;
import java.util.Base64;


@Deprecated
public interface Render extends CtxHandler<HttpCtx> {

  static CtxHandler dump(Object o) {
    return Express.express((req, resp)-> {
      resp.send(S.dump(o));
    });
  }
  static CtxHandler text(String text) {
    return ((Mid)(req, resp )-> {
      resp.contentType("text/plain;charset=utf-8");
      resp.write(text);
      resp.send(200);
    }).toCtxHandler();
  }

  static CtxHandler json(Object o) {
    return Mid.of((req, resp) -> {
      resp.contentType("application/json;charset=utf-8");
      resp.write(JSON.stringify(o));
      resp.send(200);
    }).toCtxHandler();
  }



  //TODO move to REST package
  static CtxHandler page(Object o, int totalCount) {
    return Mid.of((req, resp) -> {
      resp.contentType("application/json;charset=utf-8");
      resp.header("X-Total-Count", String.valueOf(totalCount));
      resp.write(JSON.stringify(o));
      resp.send(200);
    }).toCtxHandler();
  }

}
