package com.shuimin.jtiny;

import com.shuimin.base.S;
import com.shuimin.jtiny.codec.view.View;
import com.shuimin.jtiny.core.Dispatcher;
import com.shuimin.jtiny.core.Server;
import com.shuimin.jtiny.core.mw.Action;
import com.shuimin.jtiny.core.mw.router.Router;

import static com.shuimin.jtiny.core.Interrupt.render;
import static com.shuimin.jtiny.core.Server.BasicServer.jetty;

public class CoreTest {

    public static void simple() {
        Server.basis(jetty).use(Action.simple((req, resp) -> {
            S.echo(req);
            resp.writer().print("sddd");
        })).listen(9090);
    }


    public static void _1() {

        final Dispatcher dispatcher = new Dispatcher(new Router.RegexRouter());

        dispatcher.make(ctx -> {
            ctx.get("/", Action.simple((req, resp) -> {
                //jump();
                render(View.Text.one().text("Hello"));
            }), Action.simple((req, resp) -> render(View.Text.one().text("ejumped"))));

            ctx.get("/test", Action.supply(() -> ("TEST HELLO")),
                Action.<String>consume((hello) -> render(View.Text.one().text(hello))));
        });


        dispatcher.make(ctx -> {
            ctx.get("/${id}/${user}", Action.simple((req, resp) ->
                    render(View.Text.one().text(
                        "<p>id=" + req.param("id") + "</p>" +
                            "<p>user=" + req.param("user") + "</p>"
                    ))
            ));
        });


        //config begin

        Server.global().mode(Server.RunningMode.debug);

        Server.basis(jetty)
            .use(dispatcher).listen(9090);
    }

    public static interface SupplyString {
        public String a();
    }

    public static void echo(SupplyString su){
        S.echo(su.a());
    }

    public static void main(String[] args) {
        //_1();

    }

}
