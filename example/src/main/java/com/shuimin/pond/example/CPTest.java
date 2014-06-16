package com.shuimin.pond.example;

import com.shuimin.pond.core.Pond;
import com.shuimin.pond.core.db.DB;
import com.shuimin.pond.core.db.Record;
import com.shuimin.pond.core.mw.Action;
import com.shuimin.pond.core.mw.Dispatcher;

import java.util.List;

import static com.shuimin.pond.core.Interrupt.render;
import static com.shuimin.pond.core.Renderable.json;

/**
 * Created by ed on 6/11/14.
 */
public class CPTest {

    public static void main(String[] ss) {
        Pond.init().use(
                new Dispatcher()
                        .get("/db", Action.simple((req, resp) -> {
                            List<Record> list =
                            DB.fire(tmpl -> tmpl.find("" +
                                    "select * from test"));
                            render(json(list));
                        }))
        ).start(8080);
    }

}
