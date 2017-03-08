package pond.web;

import pond.common.Convert;
import pond.common.JSON;
import pond.common.S;
import pond.common.f.Function;

import java.util.*;

public class ParamDef<A> {
    final String name;
    Function<A, Ctx> handler;

    ParamDef(String name) {
        this.name = name;
    }

    ParamDef(String name, Function<A, Ctx> handler) {
        this.name = name;
        this.handler = handler;
    }

    public String name() {
        return this.name;
    }

    public A get(Ctx c) {
        return this.handler.apply(c);
    }

    @SuppressWarnings("unchecked")
    public ParamDef<A> required(String errMsg) {
        return new ParamDef<A>(this.name() + "_required", ctx -> {
            A ret;
            if ((ret = this.handler.apply(ctx)) == null)
                throw new EndToEndException(400, errMsg);
            return ret;
//            if (this.aClass.equals(Request.UploadFile.class)) {
//                Request.UploadFile file = ((HttpCtx) ctx).req.file(this.name());
//                if (file == null) throw new EndToEndException(400, errMsg);
//                return (A) file;
//            } else {
//                String str = ((HttpCtx) ctx).req.paramNonBlank(this.name(), errMsg);
//                if (this.aClass.equals(String.class)) return (A) str;
//                if (this.aClass.equals(Integer.class)) return (A) Convert.toInt(str);
//                if (this.aClass.equals(date.class)) return (A) Convert.toDate(Convert.toLong(str));
//                if (this.aClass.equals(Long.class)) return (A) Convert.toLong(str);
//                if (this.aClass.equals(Double.class)) return (A) Convert.toDouble(str);
//                throw new EndToEndException(400, String.format("Unsupported type: %s", this.aClass.getCanonicalName()));
//            }
        });
    }

    public static ParamDef<String> header(String name) {
        return new ParamDef<String>(name, ctx-> ((HttpCtx) ctx).req.header(name));
    }

    public static ParamDef<String> str(String name) {
        return new ParamDef<>(name, ctx -> ((HttpCtx) ctx).req.param(name));
    }

    public static ParamDef<Integer> Int(String name) {
        return new ParamDef<>(name, ctx -> Convert.toInt(((HttpCtx) ctx).req.param(name)));
    }

    public static ParamDef<Long> Long(String name) {
        return new ParamDef<>(name, ctx -> Convert.toLong(((HttpCtx) ctx).req.param(name)));
    }

    public static ParamDef<Double> Double(String name) {
        return new ParamDef<>(name, ctx -> Convert.toDouble(((HttpCtx) ctx).req.param(name)));
    }

    public static ParamDef<Date> date(String name) {
        return new ParamDef<>(name, ctx -> Convert.toDate(Convert.toLong(((HttpCtx) ctx).req.param(name))));
    }

    public static ParamDef<Map<String, Object>> requestToMap() {
        return new ParamDef<>("request_map", ctx -> (((HttpCtx) ctx).req.toMap()));
    }

    public static ParamDef<Request.UploadFile> file(String name) {
        return new ParamDef<>(name, ctx -> ((HttpCtx) ctx).req.file(name));
    }

    public static <X> ParamDef<X> any(String name, Function<X, Ctx> handler) {
        return new ParamDef<X>(name, handler);
    }

    public static <X> ParamDefStruct<X> obj(String name, List<ParamDef> defs, Function<X, Map<String, Object>> parser ) {
        return new ParamDefStruct<>(name, parser, defs);
    }

    /**
     * default to required
     *
     * @param name
     * @param itemParser
     * @param <X>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <X> ParamDef<X> json(String name, Function<X, Map<String, Object>> itemParser) {
        return new ParamDef<X>(name + "_required", ctx ->
                (X) itemParser.apply(JSON.parse(((HttpCtx) ctx).req
                        .paramNonBlank(name, String.format("%s must not null", name))
                )));
    }

    /**
     * default to required
     *
     * @param name
     * @param arrayItemParser
     * @param <X>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <X> ParamDef<List<X>> jsonArray(String name, Function<X, Map<String, Object>> arrayItemParser) {
        return new ParamDef<>(name + "_required", ctx -> {
            List<Map<String, Object>> l = JSON.parseArray(((HttpCtx) ctx).req
                    .paramNonBlank(name, String.format("%s must not null", name)));
            return S._for(l).map(arrayItemParser).toList();
        });
    }
}
