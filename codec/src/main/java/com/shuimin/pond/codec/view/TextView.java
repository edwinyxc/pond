package com.shuimin.pond.codec.view;


import com.shuimin.pond.core.Response;

/**
 * Top abstract View merge Text Value
 *
 * @author ed
 */
public class TextView extends View {

    private String text;

    protected TextView(String text) {
        this.text = text;
    }

    public static TextView one() {
        return (TextView) new TextView("").onRender(
                (resp) -> {
                    resp.contentType("text/html;charset=utf8");
                }
        );
    }

    public TextView val(String text) {
        this.text = text;
        return this;
    }

    @Override
    public void _render(Response resp) {
        resp.writer().print(text);
        resp.writer().flush();
    }
}
