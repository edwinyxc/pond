package pond.web.restful;

import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.util.CharsetUtil;
import pond.common.Convert;
import pond.common.JSON;
import pond.common.S;
import pond.common.STREAM;
import pond.common.f.Function;
import pond.web.*;
import pond.web.http.HttpCtx;
import pond.web.http.MIME;
import pond.web.router.Route;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class ParamDef<A> {
    public final String name;
    public Schema schema = Schema.STRING();
    public ParamType type;
    public ParamIn in;
    public String desc;
    public Function<A, HttpCtx> handler;
    public boolean required = false;
    public String[] consumes = {"application/x-www-form-urlencoded", "bodyAsMultipart/form-data"};

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
        COMPOSED("composed");//we compose our own, ignore OpenAPI Spec
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

    ParamDef(String name, Function<A, HttpCtx> handler) {
        this.name = name;
        desc = name;
        this.handler = handler;
    }

    ParamDef(String name, Function<A, HttpCtx> handler, boolean required) {
        this.name = name;
        desc = name;
        this.handler = handler;
        this.required = required;
    }

    public ParamDef<A> consumes(String... consumes){
        this.consumes = consumes;
        return this;
    }

    public ParamDef<A> schema(Schema schema) {
        this.schema = schema;
        return this;
    }

    public ParamDef<A> desc(String desc) {
        this.desc = desc;
        return this;
    }

    public <X> ParamDef<X> to(Function<X, A> compose) {
        return new ParamDef<>(this.name, this.handler.compose(compose), this.required);
    }

    public ParamDef<Long> toLong() {
        return to(Convert::toLong).in(this.in).type(ParamType.INTEGER);
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

    public A get(HttpCtx c) {
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
        return new ParamDef<>(name, ctx -> ((HttpCtx.Lazy) ctx::bind).req().header(name))
                .type(ParamType.STRING).in(ParamIn.HEADER)
                .consumes();
    }

    public static ParamDef<List<String>> arrayInQuery(String name) {
        return new ParamDef<>(name, ctx -> {
            List<String> originalQueries = S._for(((HttpCtx.Lazy) ctx::bind).req().queries().get(name)).toList();
            if( originalQueries.size() > 1 || originalQueries.size() == 0) {
                return originalQueries;
            }
            else return Arrays.asList(originalQueries.get(0).split(","));
        }).type(ParamType.ARRAY).in(ParamIn.QUERY);
    }

    public static ParamDef<String> query(String name) {
        return new ParamDef<>(name, ctx -> S._for(((HttpCtx.Lazy) ctx::bind).req().queries().get(name)).first())
                .type(ParamType.STRING).in(ParamIn.QUERY);
    }

    public static ParamDef<String> path(String name) {
        return new ParamDef<>(name, ctx -> S._for(((HttpCtx.Lazy) ctx::bind).req().inUrlParams().get(name)).first())
                .type(ParamType.STRING).in(ParamIn.PATH)
                .consumes().required("in-path parameters are auto-required");
    }

    public static ParamDef<List<String>> arrayInPath(String name) {
        return new ParamDef<>(name, ctx -> {
            List<String> originalQueries = S._for(((HttpCtx.Lazy) ctx::bind).req().inUrlParams().get(name)).toList();
            if( originalQueries.size() > 1 || originalQueries.size() == 0) {
                return originalQueries;
            }
            else return Arrays.asList(originalQueries.get(0).split(","));
        }).type(ParamType.ARRAY).in(ParamIn.PATH);
    }

    public static ParamDef<List<String>> arrayInForm(String name) {
        return new ParamDef<>(name, ctx -> {
            List<String> originalQueries = S._for(((HttpCtx.Lazy) ctx::bind).req().formData().get(name)).toList();
            if( originalQueries.size() > 1 || originalQueries.size() == 0) {
                return originalQueries;
            }
            else return Arrays.asList(originalQueries.get(0).split(","));
        }).type(ParamType.ARRAY).in(ParamIn.FORM_DATA);
    }

    public static ParamDef<String> form(String name) {
        return new ParamDef<>(name, ctx -> S._for(((HttpCtx.Lazy) ctx::bind).req().formData().get(name)).first())
                .type(ParamType.STRING).in(ParamIn.FORM_DATA);
    }

    public static ParamDef<String> param(String name) {
        return new ParamDef<>(name, ctx -> ((HttpCtx.Lazy) ctx::bind).req().param(name))
                .type(ParamType.STRING)
                //default to form data
                .in(ParamIn.FORM_DATA);
    }

    public static ParamDef<List<String>> params(String name) {
        return new ParamDef<>(name, ctx -> ((HttpCtx.Lazy) ctx::bind).req().params(name))
                .type(ParamType.ARRAY)
                .in(ParamIn.FORM_DATA);
    }

    public static ParamDef<Map<String,Object>> reqAsMap() {
        return new ParamDef<>("req as map", ctx -> ((HttpCtx.Lazy) ctx).req().toMap())
                .type(ParamType.SCHEMA).in(ParamIn.COMPOSED);
    }

    private static final String CACHED_BODY_AS_JSON = "CACHED_BODY_AS_JSON";

    static void cacheBodyAsJSON(HttpCtx c) throws IOException {
        c.put(CACHED_BODY_AS_JSON, JSON.parse(STREAM.readFully(((HttpCtx.Lazy) c::bind).req().in(), CharsetUtil.UTF_8)));
    }

    public static ParamDef<InputStream> bodyAsInputStream(String name) {
        return new ParamDef<>(name, ctx -> ((HttpCtx.Lazy) ctx::bind).req().in()).in(ParamIn.BODY);
    }

    public static ParamDef<Map<String, Object>> bodyAsJsonMap(String name) {
        return bodyAsX(name, map -> map);
    }

    public static <X> ParamDef<X> bodyAsX(String name, Function<X, Map<String, Object>> mapToXFn) {
        return new ParamDef<X>(name, ctx -> {
            try {
                Map cached = (Map) ctx.get(CACHED_BODY_AS_JSON);
                if(cached == null) {
                    cacheBodyAsJSON(ctx);
                    cached = (Map) ctx.get(CACHED_BODY_AS_JSON);
                }
                return mapToXFn.apply((Map<String, Object>) cached);
            } catch (IOException | RuntimeException e) {
                throw new EndToEndException(400, "parse error:" + e.getMessage());
            }
        }) .consumes(MIME.MIME_APPLICATION_JSON)
                .in(ParamIn.BODY)
                .type(ParamType.SCHEMA);
    }

//    public static ParamDefStruct<Map<String, Object>> requestToMap() {
//        return new ParamDefStruct<>("all_params",  ctx -> (((HttpCtx) ctx).req.toMap()));
//    }

    public static ParamDef<FileUpload> file(String name) {
        return new ParamDef<>(name, ctx -> ((HttpCtx.Lazy) ctx).req().file(name)).in(ParamIn.BODY).type(ParamType.FILE);
    }

    public static <X> ParamDef<X> any(String name, Function<X, HttpCtx> handler) {
        return new ParamDef<>(name, handler).in(ParamIn.QUERY).type(ParamType.STRING);
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
                def = composeAs(name, (Map<String, Object>) val);
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
    public static <X> ParamDef<X> composeAs(String name,
                                        Map<String, Object> schema,
                                        Function<X, Map<String, Object>> mapToXFunc) {
        return new ParamDefStruct<>(name, mapToXFunc, parseSchema(schema, ParamDef::param))
                .in(ParamIn.FORM_DATA)
                .type(ParamType.SCHEMA)
                .schema(Schema.OBJECT(schema))
                ;
    }

    public static ParamDef<Map<String, Object>> composeAs(String name,
                                                          Map<String, Object> schema) {
        return new ParamDefStruct<>(name, map -> map, parseSchema(schema, ParamDef::param))
                .in(ParamIn.FORM_DATA)
                .type(ParamType.SCHEMA)
                .schema(Schema.OBJECT(schema))
                ;
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
                (X) itemParser.apply(JSON.parse(((HttpCtx.Lazy) ctx::bind).req()
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
            List<Map<String, Object>> l = JSON.parseArray(((HttpCtx.Lazy) ctx::bind).req()
                    .paramNonBlank(name, String.format("%s must not null", name)));
            return S._for(l).map(arrayItemParser).toList();
        });
    }
}
