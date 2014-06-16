package com.shuimin.pond.core.mw;

import com.shuimin.common.S;
import com.shuimin.pond.core.Pond;
import com.shuimin.pond.core.mw.Action;
import com.shuimin.pond.core.mw.Dispatcher;

import static com.shuimin.pond.core.Interrupt.render;

public class CoreTest {



    public static void echo(SupplyString su) {
        S.echo(su.a());
    }

    public static void main(String[] args) {
        Dispatcher app = new Dispatcher();

        app.get("/asd", (req,resp) -> {

        });

    }

    public static interface SupplyString {
        public String a();
    }

}
