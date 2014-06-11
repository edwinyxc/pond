package com.shuimin.pond.codec.view;

import com.shuimin.common.S;
import com.shuimin.pond.core.Response;

import java.io.IOException;
import java.io.InputStream;

/**
 * Top abstract View merge Blob Value
 *
 * @author ed
 */
public class StreamView extends View {

    private final InputStream is;

    protected StreamView(InputStream is) {
        this.is = is;
    }

    public static StreamView one(InputStream is) {
        return new StreamView(is);
    }

    @Override
    public void _render(Response resp) {
        try {
            S.stream.write(is, resp.out());
        } catch (IOException ex) {
            S._lazyThrow(ex);
        }
        resp.writer().flush();
    }
}
