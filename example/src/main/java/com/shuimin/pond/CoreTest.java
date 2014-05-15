package com.shuimin.pond;

import com.shuimin.common.S;
import com.shuimin.pond.codec.view.View;
import com.shuimin.pond.core.Pond;
import com.shuimin.pond.core.mw.Action;
import com.shuimin.pond.core.mw.Dispatcher;
import com.shuimin.pond.core.mw.router.Router;

import static com.shuimin.pond.core.Interrupt.render;

public class CoreTest {



    public static void _1() {

        final Dispatcher dispatcher = new Dispatcher(new Router.RegexRouter());

        dispatcher.make(ctx -> {
            ctx.get("/", Action.simple((req, resp) -> {
                //jump();
                render(View.Text.one().val("Hello"));
            }), Action.simple((req, resp) -> render(View.Text.one().val("ejumped"))));

            ctx.get("/test", Action.supply(() -> ("TEST HELLO")),
                Action.<String>consume((hello) -> render(View.Text.one().val(hello))));
        });


        dispatcher.make(ctx -> {
            ctx.get("/${id}/${user}", Action.simple((req, resp) ->
                    render(View.Text.one().val(
                        "<p>id=" + req.param("id") + "</p>" +
                            "<p>user=" + req.param("user") + "</p>"
                    ))
            ));
        });



        Pond.init().debug()
            .use(dispatcher).start(9090);
    }

    public static interface SupplyString {
        public String a();
    }

    public static void echo(SupplyString su){
        S.echo(su.a());
    }

    public static void main(String[] args) {
        _1();

    }

}
