package pond.web.restful;

import io.netty.util.CharsetUtil;
import pond.common.Convert;
import pond.common.JSON;
import pond.common.S;
import pond.common.STREAM;
import pond.common.f.Function;
import pond.web.*;
import pond.web.http.MimeTypes;

import java.io.IOException;
import java.util.*;

public class ParamDef<A> {
    public final String name;
    public ParamType type;
    public ParamIn in;
    public String desc;
    public Function<A, Ctx> handler;
    public boolean required = false;
    public String[] consumes = {"application/x-www-form-urlencoded", "multipart/form-data"};

    public enum ParamType {
        STRING("string"),
        NUMBER("number"),
        INTEGER("integer"),
        BOOLEAN("boolean"),
        ARRAY("array"),
        FILE("file"),
        SCHEMA("schema");
        public final String val;

        ParamType(String val) {
            this.val = val;
        }
    }

    public enum ParamIn {
        FORM_DATA("formData"),
        QUERY("query"),
        HEADER("header"),
        PATH("path"),
        BODY("body"),
        COMPOSED("composed");//we compose our own, excluded against the doc
        public final String val;

        ParamIn(String val) {
            this.val = val;
        }

        public static ParamIn defaultforRoute(Route route) {
            switch (route.method) {
                case GET:
                case HEAD:
                case DELETE:
                case CONNECT:
                    return QUERY;
                case POST:
                case PUT:
                    return FORM_DATA;
                default:
                    return QUERY;
            }

        }
    }


    ParamDef(String name) {
        this.name = name;
        this.desc = name;
    }

    ParamDef(String name, Function<A, Ctx> handler) {
        this.name = name;
        desc = name;
        this.handler = handler;
    }

    ParamDef(String name, Function<A, Ctx> handler, boolean required) {
        this.name = name;
        desc = name;
        this.handler = handler;
        this.required = required;
    }

    public ParamDef<A> consumes(String... consumes){
        this.consumes = consumes;
        return this;
    }

    public ParamDef<A> desc(String desc) {
        this.desc = desc;
        return this;
    }

    public <X> ParamDef<X> to(Function<X, A> compose) {
        return new ParamDef<>(this.name, this.handler.compose(compose), this.required);
    }

    public ParamDef<Boolean> toBoolean() {
        return to(Convert::toBoolean).in(this.in).type(ParamType.BOOLEAN);
    }

    public ParamDef<Double> toNumber() {
        return to(Convert::toDouble).in(this.in).type(ParamType.NUMBER);
    }

    public ParamDef<Integer> toInteger() {
        return to(Convert::toInt).in(this.in).type(ParamType.INTEGER);
    }

    public ParamDef<A> in(ParamIn in) {
        this.in = in;
        return this;
    }

    public ParamDef<A> type(ParamType type) {
        this.type = type;
        return this;
    }

//    //JSON parsed from body
//    public ParamDef<A> schema(Map<String,Object> schema, ) {
//        this.type = ParamType.SCHEMA;
//        this.in = ParamIn.BODY;
//        this.schema = schema;
//    }

    public A get(Ctx c) {
        return this.handler.apply(c);
    }

    public ParamDef<A> required(String errMsg) {
        this.required = true;
        this.handler = this.handler.compose(a -> {
            if (a == null || a.equals("null") || a.equals("undefined")) {
                throw new EndToEndException(400, errMsg);
            } else return a;
        });
        return this;
    }

    public static ParamDef<String> header(String name) {
        return new ParamDef<>(name, ctx -> ((HttpCtx) ctx).req.header(name))
                .type(ParamType.STRING).in(ParamIn.HEADER)
                .consumes();
    }

    public static ParamDef<String> query(String name) {
        return new ParamDef<>(name, ctx -> S._for(((HttpCtx) ctx).req.queries().get(name)).first())
                .type(ParamType.STRING).in(ParamIn.QUERY);
    }

    public static ParamDef<String> path(String name) {
        return new ParamDef<>(name, ctx -> S._for(((HttpCtx) ctx).req.inUrlParams().get(name)).first())
                .type(ParamType.STRING).in(ParamIn.PATH)
                .consumes().required("in-path parameters are auto-required");
    }

    public static ParamDef<String> form(String name) {
        return new ParamDef<>(name, ctx -> S._for(((HttpCtx) ctx).req.formData().get(name)).first())
                .type(ParamType.STRING).in(ParamIn.FORM_DATA);
    }

    public static ParamDef<String> param(String name) {
        return new ParamDef<>(name, ctx -> ((HttpCtx) ctx).req.param(name))
                .type(ParamType.STRING)
                //default to form data
                .in(ParamIn.FORM_DATA);
    }

    public static ParamDef<Map<String,Object>> reqAsMap() {
        return new ParamDef<>("req as map", ctx -> ((HttpCtx) ctx).req.toMap())
                .type(ParamType.SCHEMA).in(ParamIn.COMPOSED);
    }

    private static final String CACHED_BODY_AS_JSON = "CACHED_BODY_AS_JSON";

    static void cacheBodyAsJSON(Ctx c) throws IOException {
        c.put(CACHED_BODY_AS_JSON, JSON.parse(STREAM.readFully(((HttpCtx) c).req.in(), CharsetUtil.UTF_8)));
    }

    static <X> X getFromCachedBody(Ctx c, String name) throws IOException {
        Map<String, Object> cache = (Map<String, Object>) c.get(CACHED_BODY_AS_JSON);
        if (cache == null) {
            cacheBodyAsJSON(c);
            return getFromCachedBody(c, name);
        }
        return (X) cache.getOrDefault(name, null);
    }

    //non-public to ensure swagger policy
    static <X> ParamDef<X> parseBodyAndGet(String name) {
        return new ParamDef<X>(name, ctx -> {
            try {
                return getFromCachedBody(ctx, name);
            } catch (IOException | RuntimeException e) {
                throw new EndToEndException(400, "parse error:" + e.getMessage());
            }
        }).consumes(MimeTypes.MIME_APPLICATION_JSON).in(ParamIn.BODY).type(ParamType.STRING);
    }

//    public static ParamDefStruct<Map<String, Object>> requestToMap() {
//        return new ParamDefStruct<>("all_params",  ctx -> (((HttpCtx) ctx).req.toMap()));
//    }

    public static ParamDef<Request.UploadFile> file(String name) {
        return new ParamDef<>(name, ctx -> ((HttpCtx) ctx).req.file(name)).in(ParamIn.BODY).type(ParamType.FILE);
    }

    public static <X> ParamDef<X> any(String name, Function<X, Ctx> handler) {
        return new ParamDef<X>(name, handler).in(ParamIn.QUERY).type(ParamType.STRING);
    }

    public static <X> ParamDef<X> compose(String name, List<ParamDef> defs, Function<X, Map<String, Object>> parser) {
        return new ParamDefStruct<>(name, parser, defs).in(ParamIn.COMPOSED).type(ParamType.SCHEMA);
    }

    @SuppressWarnings("unchecked")
    private static List<ParamDef> parseSchema(Map<String, Object> schema, Function<ParamDef<?>, String> itemDef) {
        String name;
        Object val;
        ParamDef def;
        List<ParamDef> ret = new LinkedList<>();
        for (Map.Entry<String, Object> entry : schema.entrySet()) {
            name = entry.getKey();
            val = entry.getValue();
            if (val instanceof Map) {
                def = composeAs(name, (Map<String, Object>) val, itemDef);
            } else {
                def = itemDef.apply(name);
            }
            ret.add(def);
        }
        return ret;
    }


    /**
     * Compose to a schema
     *
     * @param name
     * @param schema
     * @return
     */
    public static ParamDef<Map<String, Object>> composeAs(String name,
                                                          Map<String, Object> schema,
                                                          Function<ParamDef<?>, String> itemDef) {
        return new ParamDefStruct<>(name, map -> map, parseSchema(schema, itemDef))
                .in(ParamIn.COMPOSED)
                .type(ParamType.SCHEMA);
    }

    public static ParamDef<Map<String, Object>> composeAs(String name,
                                                          Map<String, Object> schema) {
        return new ParamDefStruct<>(name, map -> map, parseSchema(schema, ParamDef::param))
                .in(ParamIn.COMPOSED)
                .type(ParamType.SCHEMA);
    }

    public static ParamDef<Map<String, Object>> composeBodyAs(String name,
                                                          Map<String, Object> schema) {
        return new ParamDefStruct<>(name, map -> map, parseSchema(schema, ParamDef::parseBodyAndGet))
                .in(ParamIn.BODY)
                .type(ParamType.SCHEMA);
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
