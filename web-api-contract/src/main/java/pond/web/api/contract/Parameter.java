package pond.web.api.contract;

import io.netty.handler.codec.http.cookie.Cookie;
import pond.common.S;
import pond.common.SPILoader;
import pond.common.f.Callback;
import pond.common.f.FIterable;
import pond.common.f.Function;
import pond.core.Ctx;
import pond.core.CtxHandler;
import pond.core.Entry;
import pond.web.Request;
import pond.web.Response;
import pond.web.http.HttpCtx;

import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Parameter<T> {

    public enum IN {
        ANY, QUERIES, PATH, HEADER, COOKIE, BODY_XML, BODY_JSON, BODY_FORM, BODY_RAW, CTX
    }

    public enum SCHEMA {
        ANY, STR, STR_DATE, LONG_DATE, INT, BOOL, NUMBER, ARRAY, REQ, RESP, CTX
    }

    public final Entry<T> entry;
    public final String name;
    public final Type<T> type;
    SCHEMA schema;
    IN in;
    public final boolean required;


    private final SupportedTypes supportedTypes = SPILoader.service(SupportedTypes.class);

    private List<Annotation> affectiveAnnotations = new LinkedList<>();

    Function<Object, HttpCtx> provider;
    Function<T, Object> converter;

    private List<Callback.C2<Parameter<T>, FIterable<Annotation>>> onAnnotations = new LinkedList<>();

    public Parameter(String name, Type<T> type, SCHEMA schema, IN in, boolean required) {
        this.name = name;
        this.type = type;
        Class<T> reifiedType = type.reifiedType();
        if(Ctx.class.isAssignableFrom(reifiedType)){
            this.entry = (Entry<T>) Ctx.SELF;
            this.in = IN.CTX;
            this.schema = SCHEMA.CTX;
            this.provider = ctx -> ctx.get(entry);
            this.converter = t -> (T) t;
            this.required = true;
            return;
        }else if(Request.class.isAssignableFrom(reifiedType)){
            this.entry = (Entry<T>) HttpCtx.REQ;
            this.in = IN.CTX;
            this.schema = SCHEMA.REQ;
            this.provider = ctx -> ctx.get(entry);
            this.converter = t -> (T) t;
            this.required = true;
            return;
        }else if(Response.class.isAssignableFrom(reifiedType)){
            this.entry = (Entry<T>) HttpCtx.RESP;
            this.in = IN.CTX;
            this.schema = SCHEMA.RESP;
            this.provider = ctx -> ctx.get(entry);
            this.converter = t -> (T) t;
            this.required = true;
            return;
        }else {
            Entry<T> canonicalEntry = new Entry<>(name + ":" + reifiedType.getCanonicalName());
            this.entry = canonicalEntry;
            this.in = IN.ANY;
            this.schema = SCHEMA.ANY;
        }
        this.required = required;

        if(schema != null && schema != SCHEMA.ANY) this.schema = schema;
        if(in != null && in != IN.ANY) this.in = in;

        //build provider
        if (in == Parameter.IN.PATH) {
                    provider = ctx -> S._for((((HttpCtx.Queries) ctx::bind).inUrlParams().get(name))).first();
                } else if (in == Parameter.IN.QUERIES) {
                    provider = ctx -> (((HttpCtx.Queries) ctx::bind).query(name));
                } else if (in == Parameter.IN.COOKIE) {
                    var CookieName = ((ParameterInContract.Cookie) a).value();
                    valueProvider = ctx -> Optional.of(((HttpCtx.Cookies) ctx::bind).cookie(CookieName))
                            .map(Cookie::value).orElse(null);
                } else if (in == HEADER) {
                    valueProvider = ctx -> S._for((((HttpCtx.Headers) ctx::bind).headers().get(name))).first();
                } else if (in == Parameter.IN.BODY_FORM) {
                    valueProvider = ctx -> {
                        var c = (HttpCtx.Body) ctx::bind;
                        try {
                            return S._for(c.bodyAsMultipart().attrs().get(name)).first();
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    };
                } else if (in == Parameter.IN.BODY_XML) {
                    //TODO
                    throw new RuntimeException("BODY_XML is not supported yet");
                } else if (in == Parameter.IN.BODY_JSON) {
                    provider = ctx -> {
                        var c = (HttpCtx.Body) ctx::bind;
                        return Optional.of(c.bodyAsJson()).map(m -> m.get(name)).orElse(null);
                    };
                } else if (in == Parameter.IN.BODY_RAW) {
                    valueProvider = ctx -> {
                        var c = (HttpCtx.Body) ctx::bind;
                        return c.bodyAsRaw();
                    };
                } else {
                    //TODO solve this
                    valueProvider = ctx -> {
                        var c = (HttpCtx.Lazy) ctx::bind;
                        return Optional.ofNullable(c.req().params().get(name)).map(m -> m.get(0))
                                .orElse(null);
                    };
                }
        //build converter

    }

    //for custom types
    public Parameter<T> readAnnotations(Annotation[] annos) {
        S._for(onAnnotations).each(h -> h.apply(this, S._for(annos)));
        return this;
    }

    public CtxHandler provider(){
        //provider - converter -
        return CtxHandler.process(
            Ctx.SELF,
            ctx -> converter.apply(provider.apply(ctx::bind)),
            this.entry);
    }


}
