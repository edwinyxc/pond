package pond.core;

import pond.common.*;
import pond.core.http.MimeTypes;
import pond.core.spi.ViewEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static pond.core.Pond.debug;

/**
 * Created by ed on 2014/4/18.
 */
public interface Render {

    Logger logger = LoggerFactory.getLogger(Render.class);

    @Deprecated
    static Render error(int err_code, String msg) {
        String path = "err" + File.separator + err_code;
        return view(path, new HashMap<String, Object>() {{
            this.put("err_msg", msg);
        }});
    }

    static Render text(String text) {
        return (req, resp) -> {
            resp.write(text);
            resp.send(200);
        };
    }

    static Render json(Object o) {
        //JsonService serv = SPILoader.service(JsonService.class);
        return (req, resp) -> {
            resp.contentType("application/json;charset=utf-8");
            resp.write(JSON.stringify(o));
            resp.send(200);
        };
    }

    @Deprecated
    static Render file(File f) {
        return (req, resp) -> {
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
        };
    }

    /**
     * download
     */
    @Deprecated
    static Render attachment(InputStream file, String filename) {
        return (req, resp) -> {
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
                        ||agent.toLowerCase().contains("safari")) {
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
                S.stream.pipe(file, resp.out());
                resp.out().flush();
                //resp.sendFile(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    static Render dump(Object o) {
        return (req, resp) ->
                resp.write(S.dump(o));
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    static Render view(String path, Object o) {

        return (req, resp) -> {
            Pond app = req.ctx().pond;
            File file = new File(app.attr(
                    Config.VIEWS_PATH) + File.separator + path);
            /**
             * v1.1.0 add ext-based engine
             */
            String ext = S.file.fileExt(path);
            ViewEngine engine;
            if (S.str.notBlank(ext)) {
                engine = app.viewEngine(ext);
            } else {
                engine = app.viewEngine("default");
            }

            if (file.exists()) {
                final Object render;
                //copy
                Map map = new HashMap(req.ctx());
                if (o == null) {
                    render = map;
                } else if (o instanceof Map) {
                    map.putAll((Map) o);
                    render = map;
                } else {
                    render = o;
                }
                debug("Render-Object:" + S.dump(render));
                try {
                    engine.render(resp.out(), path, render);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                logger.warn("File" + file + "not found");
                json(o).render(req, resp);
            }
        };
    }

    static Render view(String path) {
        return view(path, null);
    }

    void render(Request req, Response resp);


}
