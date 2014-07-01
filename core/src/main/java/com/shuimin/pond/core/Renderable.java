package com.shuimin.pond.core;

import com.shuimin.common.S;
import com.shuimin.pond.core.kernel.PKernel;
import com.shuimin.pond.core.misc.MimeTypes;
import com.shuimin.pond.core.spi.JsonService;
import com.shuimin.pond.core.spi.ViewEngine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static com.shuimin.common.S._for;
import static com.shuimin.pond.core.Pond.*;

/**
 * Created by ed on 2014/4/18.
 */
public interface Renderable {

    public static Renderable error(int err_code, String msg) {
        String path = "err" + File.separator + err_code;
        return view(path, new HashMap<String, Object>() {{
            this.put("err_msg", msg);
        }});
    }

    public static Renderable text(String text) {
        return (resp) -> {
            resp.write(text);
            resp.send(200);
        };
    }

    public static Renderable json(Object o) {
        JsonService serv = PKernel.getService(JsonService.class);
        return (resp) -> {
            resp.contentType("application/json;charset=utf-8");
            resp.write(serv.toString(o));
            resp.send(200);
        };
    }

    public static Renderable file(InputStream file, String filename) {
        return resp -> {
            String filen = S.str.notBlank(filename) ? filename :
                    String.valueOf(S.time());
            String filen_ext = S.file.fileExt(filen);
            String mime_type;

            //TODO refactor
            if (filen_ext != null
                    && (mime_type = MimeTypes.getMimeType(filen_ext)) != null) {
                resp.header("Content-Type", mime_type + ";charset=utf-8");
            } else
                resp.header("Content-Type",
                        "application/octet-stream");
            try {
                String agent = _for(REQ().header("User-Agent")).first();
                String encodedFileName;
                if (agent.contains("MSIE")) {
                    encodedFileName = URLEncoder.encode(filename, "UTF-8");
                } else {
                    encodedFileName = "=?UTF-8?B?" +
                            (new String(Base64.getEncoder().encode(filename.getBytes("UTF-8")))) + "?=";
                }
                resp.header("Content-Disposition",
                        "attachment;filename=" + encodedFileName);

            } catch (UnsupportedEncodingException ignored) {
            }
            try {
                S.stream.write(file, resp.out());
                resp.send(200);
            } catch (IOException e) {
                S._lazyThrow(e);
            }
        }

                ;
    }

    public static Renderable dump(Object o) {
        return resp ->
                resp.write(S.dump(o));
    }

    @SuppressWarnings("unchecked")
    public static Renderable view(String path, Object o) {
        File file = new File((String) Pond.attribute(Global.TEMPLATE_PATH)
                + File.separator + path);
        ViewEngine engine = PKernel.getService(ViewEngine.class);
        if (file.exists()) {
            final Object render;
            Map map = new HashMap(CUR().attrs());
            debug("Render:" + S.dump(o));
            if (o == null) {
                render = map;
            } else if (o instanceof Map) {
                map.putAll((Map) o);
                render = map;
            } else {
                render = o;
            }
            return (resp) -> engine.render(resp.out(), path, render);
        } else {
            PKernel.getLogger().warn("File" + file + "not found");
            return json(o);
        }
    }

    public static Renderable view(String path) {
        return view(path, null);
    }

    public void render(Response resp);


}
