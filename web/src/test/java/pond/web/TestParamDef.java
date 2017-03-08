package pond.web;

import pond.common.JSON;
import pond.common.S;

import java.util.HashMap;
import java.util.Map;

import static pond.web.ParamDef.*;

/**
 * Created by ed on 3/5/17.
 */
public class TestParamDef {

    static class Inner2 {
        String name;

        @Override
        public String toString() {
            return "Inner2{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }

    static class Inner {
        String name;
        Inner2 value;

        @Override
        public String toString() {
            return "Inner{" +
                    "name='" + name + '\'' +
                    ", value=" + value +
                    '}';
        }
    }

    static class DummyComplexRecord {
        String name;
        Inner value;
        long time;

        @Override
        public String toString() {
            return "DummyComplexRecord{" +
                    "name='" + name + '\'' +
                    ", value=" + value +
                    ", time=" + time +
                    '}';
        }
    }

    public static void main(String[] args) {

        Pond.init()
                .debug(Router.class, Pond.class)
                .cleanAndBind(p -> {

                    p.get("/custom_record", CtxHandler.def(
                            ParamDef.any("order", ctx -> {
                                Map ret = new HashMap();
                                ret.putAll(((HttpCtx) ctx).req.toMap());
                                return ret;
                            }),
                            ResultDef.any(200, "suc", (ctx, t) -> ((HttpCtx) ctx).resp.send(200, JSON.stringify(t))),
                            (ctx, order, suc) -> {
                                ctx.result(suc, order);
                            }
                    ));

                    p.get("/complex/", CtxHandler.def(
                            obj("complex", S.array(
                                    str("name"),
                                    obj("inner", S.array(
                                            str("inner_name"),
                                            obj("inner2", S.array(
                                                    str("inner2_name")
                                            ), map -> {
                                                Inner2 inner2 = new Inner2();
                                                inner2.name = (String) map.get("inner2_name");
                                                return inner2;
                                            })
                                    ), map -> {
                                        Inner inner = new Inner();
                                        inner.name = (String) map.get("inner_name");
                                        inner.value = (Inner2) map.get("inner2");
                                        return inner;
                                    })
                            ), map -> {
                                DummyComplexRecord record = new DummyComplexRecord();
                                record.name = (String) map.get("name");
                                record.value = (Inner) map.get("inner");
                                record.time = S.now();
                                return record;
                            }),
                            ResultDef.any(200, "success", (ctx, t) -> ((HttpCtx) ctx).resp.send(200, JSON.stringify(t))),
                            ResultDef.any(400, "parse error", (ctx, t) -> ((HttpCtx) ctx).resp.sendError(400, S.dump(t))),
                            (ctx, record, suc, err) -> {
                                ctx.result(suc, record);
                            }
                    ));

                    p.get("/well", CtxHandler.def(
                            ParamDef.str("q"),
                            ResultDef.ok(),
                            ResultDef.error(400,"Not Found"),
                            ResultDef.error(500,"Error 500"),
                            (ctx, t1, ok, not_found, internal_err) -> {
                                if (t1.equals("1")) ctx.result(ok);
                                else if (t1.equals("2")) ctx.result(not_found,"");
                                ctx.result(internal_err,"unhandled");
                            }
                    ));


//                    p.get("/work/:id", CtxHandler.any(
//                            ParamDef.any("name", ctx -> ((HttpCtx) ctx).req.param("name")),
//                            (ctx, name) -> {
//                                HttpCtx httpCtx = (HttpCtx) ctx;
//                                httpCtx.resp.send(200, name + " " + ctx.route.toString());
//                                //err
//                                return Error(4001001)
//                                //suc
//                                return Error(500,,,);
//                            },
//                            Either<A,B,C,D,E4>
//                            (ctx, result) -> {
//                                case resutlt
//                                    ctx.send()
//                            }
//                    ),RenderError(any(400,),any(500)));
//
//                    p.get("/:name", CtxHandler.any(
//                            ParamDef.Int("len"),
//                            ParamDef.str("name").required("name must not null"),
//                            (ctx, len, name) -> {
//                                HttpCtx httpCtx = (HttpCtx) ctx;
//                                httpCtx.resp.send(200, name + len + ", " + ctx.route.toString());
//                            }
//                    ));


                }).listen();

    }
}
