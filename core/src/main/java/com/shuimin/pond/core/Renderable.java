package com.shuimin.pond.core;

import com.shuimin.common.S;
import com.shuimin.pond.core.kernel.PKernel;
import com.shuimin.pond.core.spi.JsonService;
import com.shuimin.pond.core.spi.ViewEngine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

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

    public static Renderable dump(Object o) {
        return resp ->
                resp.write(S.dump(o));
    }

    public static Renderable stream(InputStream in) {
        return (resp) -> {
            try (InputStream _in = in) {
                S.stream.write(_in, resp.out());
            } catch (IOException e) {
                S._throw(e);
            }
            resp.send(200);
        };
    }

    public static Renderable view(String path, Object o) {
        File file = new File((String) Pond.attribute(Global.TEMPLATE_PATH)
                + File.separator + path);
        ViewEngine engine = PKernel.getService(ViewEngine.class);
        if (file.exists()) {
            return (resp) -> engine.render(resp.out(), path, o);
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
