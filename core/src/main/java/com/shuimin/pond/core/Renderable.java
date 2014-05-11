package com.shuimin.pond.core;

import com.shuimin.common.S;

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


//    public static Renderable json(String text) {
//    }

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

    /**
     * <p>file under ROOT</p>
     * @param file
     * @return
     */
    public static Renderable view(String file) {
        return null;
    }


    public static Renderable view(String tmpl, Object o) {
       //TODO
        throw new RuntimeException("unfinished");
    }



}
