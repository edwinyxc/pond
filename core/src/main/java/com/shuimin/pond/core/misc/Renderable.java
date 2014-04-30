package com.shuimin.pond.core.misc;

import com.shuimin.common.S;
import com.shuimin.pond.core.http.Response;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ed on 2014/4/18.
 */
public interface Renderable {
    public void render(Response resp);

    public static Renderable text(String text) {
        return (resp) -> {
            resp.write(text);
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
}
