package com.shuimin.jtiny.core.misc;

import com.shuimin.base.S;
import com.shuimin.jtiny.core.http.Response;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ed on 2014/4/18.
 */
public interface Renderable {
    public void render(Response resp);

    public static Renderable text(String text) {
        return (resp) -> {
            resp.writer().print(text);
            resp.send(200);
        };
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
