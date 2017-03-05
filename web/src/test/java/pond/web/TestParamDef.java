package pond.web;

import pond.common.Convert;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ed on 3/5/17.
 */
public class TestParamDef {
    public static void main(String[] args) {

        Pond.init(p -> {

            p.get("/custom_record", CtxHandler.def(
                    ParamDef.raw("order", Map.class, ctx -> {
                        Map ret = new HashMap();
                        ret.putAll(((HttpCtx) ctx).req.toMap());
                        return ret;
                    }),
                    (ctx, order) -> {
                        HttpCtx httpCtx = (HttpCtx) ctx;
                        httpCtx.resp.send(200, order.entrySet() + " " + ctx.route.toString());
                    }
            ));

            p.get("/", CtxHandler.def(
                    ParamDef.param("1"),
                    ParamDef.param("2"),
                    ParamDef.param("3"),
                    ParamDef.param("4"),
                    ParamDef.param("5"),
                    ParamDef.param("6"),
                    ParamDef.param("7"),
                    ParamDef.param("8"),
                    ParamDef.param("9"),
                    ParamDef.param("10"),
                    ParamDef.param("12"),
                    ParamDef.param("13"),
                    ParamDef.param("14"),
                    ParamDef.param("15"),
                    ParamDef.param("16"),
                    ParamDef.param("17"),
                    ParamDef.param("18"),
                    ParamDef.param("19"),
                    ParamDef.param("20"),
                    ParamDef.param("21"),
                    ParamDef.param("22"),
                    ParamDef.param("23"),
                    ParamDef.param("24"),
                    (ctx, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21, p22, p23) -> {

                    }

            ));

            p.get("/", CtxHandler.def(
                    ParamDef.raw("name", String.class, ctx -> ((HttpCtx) ctx).req.param("name")),
                    (ctx, name) -> {
                        HttpCtx httpCtx = (HttpCtx) ctx;
                        httpCtx.resp.send(200, name + " " + ctx.route.toString());
                    }
            ));

            p.get("/", CtxHandler.def(
                    ParamDef.paramInt("len"),
                    ParamDef.param("name").required("name must not null"),
                    (ctx, len, name) -> {
                        HttpCtx httpCtx = (HttpCtx) ctx;
                        httpCtx.resp.send(200, name + len + ", " + ctx.route.toString());
                    }
            ));


        }).debug(Pond.class).listen();

    }
}
