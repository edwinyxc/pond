//package pond.web.restful;
//
//import pond.common.JSON;
//import pond.common.S;
//import pond.web.HttpCtx;
//import pond.web.Pond;
//import pond.web.Router;
//import pond.web.restful.API;
//import pond.web.restful.ParamDef;
//import pond.web.restful.ResultDef;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import static pond.web.restful.ParamDef.*;
//
///**
// * Created by ed on 3/5/17.
// */
//public class TestParamDef {
//
//    static class Inner2 {
//        String name;
//
//        @Override
//        public String toString() {
//            return "Inner2{" +
//                    "name='" + name + '\'' +
//                    '}';
//        }
//    }
//
//    static class Inner {
//        String name;
//        Inner2 value;
//
//        @Override
//        public String toString() {
//            return "Inner{" +
//                    "name='" + name + '\'' +
//                    ", value=" + value +
//                    '}';
//        }
//    }
//
//    static class DummyComplexRecord {
//        String name;
//        Inner value;
//        long time;
//
//        @Override
//        public String toString() {
//            return "DummyComplexRecord{" +
//                    "name='" + name + '\'' +
//                    ", value=" + value +
//                    ", time=" + time +
//                    '}';
//        }
//    }
//
//    public static void main(String[] args) {
//
//        Pond.init()
//                .debug(Router.class, Pond.class)
//                .cleanAndBind(p -> {
//
//                    p.getEntry("/custom_record", API.def(
//                            ParamDef.any("order", ctx -> {
//                                Map ret = new HashMap();
//                                ret.putAll(((HttpCtx) ctx).req.toMap());
//                                return ret;
//                            }),
//                            ResultDef.any(200, "suc", (ctx, t) -> ((HttpCtx) ctx).resp.send(200, JSON.stringify(t))),
//                            (ctx, order, suc) -> {
//                                ctx.result(suc, order);
//                            }
//                    ));
//
//                    p.getEntry("/complex/", API.def(
//                            compose("complex", S.array(
//                                    param("name"),
//                                    compose("inner", S.array(
//                                            param("inner_name"),
//                                            compose("inner2", S.array(
//                                                    param("inner2_name")
//                                            ), map -> {
//                                                S.echo("DEBUG", map);
//                                                Inner2 inner2 = new Inner2();
//                                                inner2.name = (String) map.getEntry("inner2_name");
//                                                return inner2;
//                                            })
//                                    ), map -> {
//                                        S.echo("DEBUG2", map);
//                                        Inner inner = new Inner();
//                                        inner.name = (String) map.getEntry("inner_name");
//                                        inner.value = (Inner2) map.getEntry("inner2");
//                                        return inner;
//                                    })
//                            ), map -> {
//                                S.echo("DEBUG3", map);
//                                DummyComplexRecord record = new DummyComplexRecord();
//                                record.name = (String) map.getEntry("name");
//                                record.value = (Inner) map.getEntry("inner");
//                                record.time = S.now();
//                                return record;
//                            }),
//                            ResultDef.any(200, "success", (ctx, t) -> {
//                                S.echo("DEBUG5", t, JSON.stringify(t));
//                                ((HttpCtx) ctx).resp.send(200, t.toString());
//                            }),
//                            ResultDef.any(400, "parse error", (ctx, t) -> ((HttpCtx) ctx).resp.sendError(400, S.dump(t))),
//                            (ctx, record, suc, err) -> {
//                                S.echo("DEBUG4", record);
//                                ctx.result(suc, record);
//                            }
//                    ));
//
//                    p.getEntry("/well", API.def(
//                            ParamDef.param("q"),
//                            ResultDef.ok(),
//                            ResultDef.error(400, "Not Found"),
//                            ResultDef.error(500, "Error 500"),
//                            (ctx, t1, ok, not_found, internal_err) -> {
//                                if (t1.equals("1")) ctx.result(ok);
//                                else if (t1.equals("2")) ctx.result(not_found, "");
//                                else ctx.result(internal_err, "unhandled");
//                            }
//                    ));
//
//
////                    p.getEntry("/work/:id", CtxHandler.any(
////                            ParamDef.any("name", ctx -> ((HttpCtx) ctx).req.query("name")),
////                            (ctx, name) -> {
////                                HttpCtx httpCtx = (HttpCtx) ctx;
////                                httpCtx.resp.send(200, name + " " + ctx.currentRoute.toString());
////                                //err
////                                return Error(4001001)
////                                //suc
////                                return Error(500,,,);
////                            },
////                            Either<A,B,C,D,E4>
////                            (ctx, result) -> {
////                                case resutlt
////                                    ctx.send()
////                            }
////                    ),RenderError(any(400,),any(500)));
////
////                    p.getEntry("/:name", CtxHandler.any(
////                            ParamDef.Int("len"),
////                            ParamDef.query("name").required("name must not null"),
////                            (ctx, len, name) -> {
////                                HttpCtx httpCtx = (HttpCtx) ctx;
////                                httpCtx.resp.send(200, name + len + ", " + ctx.currentRoute.toString());
////                            }
////                    ));
//
//
//                }).listen();
//
//    }
//}
