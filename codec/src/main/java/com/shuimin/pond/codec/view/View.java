package com.shuimin.pond.codec.view;

import com.shuimin.common.f.Callback;
import com.shuimin.pond.core.http.Response;
import com.shuimin.pond.core.misc.Renderable;

import java.io.InputStream;

/**
 * @author ed
 */
public abstract class View implements Renderable {

    private Callback<Response> _before
        = t -> {};

    @Override
    public void render(Response resp) {
        _before.apply(resp);
        _render(resp);
    }

    public final View onRender(Callback<Response> before) {
        _before = before;
        return this;
    }

    protected abstract void _render(Response resp);

    public static class Text {

        public static TextView one() {
            return TextView.one();
        }
    }

    public static class Html {

        public static TextView one() {
            return (TextView) TextView.one().onRender((Response resp) -> {
                resp.contentType("text/html;charset=utf8");
            });
        }
    }

    public static class Blob {

        public static View one(InputStream is) {
            return StreamView.one(is);
        }
    }

}
