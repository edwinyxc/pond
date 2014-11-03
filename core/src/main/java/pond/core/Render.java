package pond.core;

import pond.common.S;
import pond.common.SPILoader;
import pond.core.misc.MimeTypes;
import pond.common.spi.JsonService;
import pond.core.spi.ViewEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static pond.common.S._for;
import static pond.core.Pond.debug;

/**
 * Created by ed on 2014/4/18.
 */
public interface Render {

    static Logger logger = LoggerFactory.getLogger(Render.class);

    public static Render error(int err_code, String msg) {
        String path = "err" + File.separator + err_code;
        return view(path, new HashMap<String, Object>() {{
            this.put("err_msg", msg);
        }});
    }

    public static Render text(String text) {
        return (req, resp) -> {
            resp.write(text);
            resp.send(200);
        };
    }

    public static Render json(Object o) {
        JsonService serv = SPILoader.service(JsonService.class);
        return (req, resp) -> {
            resp.contentType("application/json;charset=utf-8");
            resp.write(serv.toString(o));
            resp.send(200);
        };
    }

    public static Render file(File f) {
        return (req, resp) -> {
            String filename = f.getName();
            String file_n = S.str.notBlank(filename) ? filename :
                    String.valueOf(S.time());
            String file_ext = S.file.fileExt(file_n);
            String mime_type;
            if (file_ext != null
                    && (mime_type = MimeTypes.getMimeType(file_ext)) != null) {
                resp.header("Content-Type", mime_type + ";charset=utf-8");
            } else
                resp.header("Content-Type",
                        "application/octet-stream");
            try {
                S.stream.write(new FileInputStream(f), resp.out());
                resp.out().flush();
            } catch (IOException e) {
                S._lazyThrow(e);
            }
        };
    }

    /**
     * download
     */
    public static Render attachment(InputStream file, String filename) {
        return (req, resp) -> {
            String file_n = S.str.notBlank(filename) ? filename :
                    String.valueOf(S.time());
            String file_ext = S.file.fileExt(file_n);
            String mime_type;

            //TODO refactor
            if (file_ext != null
                    && (mime_type = MimeTypes.getMimeType(file_ext)) != null) {
                resp.header("Content-Type", mime_type + ";charset=utf-8");
            } else
                resp.header("Content-Type",
                        "application/octet-stream");
            try {
                String agent = _for(req.header("User-Agent")).first();
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
                S.stream.write(file, resp.out());
                resp.out().flush();
            } catch (IOException e) {
                S._lazyThrow(e);
            }
        };
    }

    public static Render dump(Object o) {
        return (req, resp) ->
                resp.write(S.dump(o));
    }

    @SuppressWarnings("unchecked")
    public static Render view(String path, Object o) {

        return (req, resp) -> {
            Pond app = req.ctx().pond;
            File file = new File((String) app.attr(
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

    public static Render view(String path) {
        return view(path, null);
    }

    public void render(Request req, Response resp);


}
