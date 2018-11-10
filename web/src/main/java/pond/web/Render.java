package pond.web;

import pond.common.*;
import pond.web.http.MimeTypes;

import java.io.*;
import java.net.URLEncoder;
import java.util.Base64;

import static pond.web.CtxHandler.express;

public interface Render extends CtxHandler{

  static CtxHandler dump(Object o) {
    return express((req, resp)-> {
      resp.send(S.dump(o));
    });
  }

  static CtxHandler text(String text) {
    return express( (req, resp )-> {
      resp.contentType("text/plain;charset=utf-8");
      resp.write(text);
      resp.send(200);
    });
  }

  static CtxHandler json(Object o) {
    return express( (req, resp) -> {
      resp.contentType("application/json;charset=utf-8");
      resp.write(JSON.stringify(o));
      resp.send(200);
    });
  }

  static CtxHandler page(Object o, int totalCount) {
    return express((req, resp) -> {
      resp.contentType("application/json;charset=utf-8");
      resp.header("X-Total-Count", String.valueOf(totalCount));
      resp.write(JSON.stringify(o));
      resp.send(200);
    });
  }

  static CtxHandler file(File f) {
    return express((req, resp) -> {
      String filename = f.getName();
      String file_n = STRING.notBlank(filename) ? filename :
          String.valueOf(S.time());
      String file_ext = FILE.fileExt(file_n);
      String mime_type;
      if (file_ext != null
          && (mime_type = MimeTypes.getMimeType(file_ext)) != null) {
        resp.header("Content-Type", mime_type + ";charset=utf-8");
      } else
        resp.header("Content-Type",
                    "application/octet-stream");
      try {
        STREAM.pipe(new FileInputStream(f), resp.out());
        resp.out().flush();
      } catch (IOException e) {
        S._throw(e);
      }
    });
  }

  /**
   * download
   */
  @Deprecated
  static CtxHandler attachment(InputStream file, String filename) {
    return express((req, resp) -> {
      String file_n = STRING.notBlank(filename) ? filename :
          String.valueOf(S.now());
      String file_ext = FILE.fileExt(file_n);
      String mime_type;

      //TODO refactor
      if (file_ext != null
          && (mime_type = MimeTypes.getMimeType(file_ext)) != null) {
        resp.header("Content-Type", mime_type + ";charset=utf-8");
      } else
        resp.header("Content-Type",
                    "application/octet-stream");
      try {
        String agent = req.header("User-Agent");
        String encodedFileName;
        if (agent.toLowerCase().contains("msie")
            || agent.toLowerCase().contains("safari")) {
          encodedFileName = URLEncoder.encode(file_n, "UTF-8");
        } else {
          encodedFileName = "=?UTF-8?B?"
              + new String(Base64.getEncoder().encode(file_n.getBytes("UTF-8")))
              + "?=";
        }
        resp.header("Content-Disposition",
                    "attachment;filename=" + encodedFileName);

      } catch (UnsupportedEncodingException ignored) {
      }
      try {
        STREAM.pipe(file, resp.out());
        resp.out().flush();
        //resp.sendFile(file);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

}
