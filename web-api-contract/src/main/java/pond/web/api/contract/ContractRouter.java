package pond.web.api.contract;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.cookie.Cookie;
import pond.common.Convert;
import pond.common.S;
import pond.common.f.Function;
import pond.common.f.Tuple;
import pond.core.Ctx;
import pond.core.CtxHandler;
import pond.core.Entry;
import pond.web.EndToEndException;
import pond.web.Request;
import pond.web.Response;
import pond.web.http.HttpCtx;
import pond.web.router.HttpMethod;
import pond.web.router.Router;
import pond.web.router.RouterAPI;
import pond.web.router.RouterCtx;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static pond.common.f.Tuple.pair;
import static pond.web.api.contract.ParameterObject.IN.CTX;
import static pond.web.api.contract.ParameterObject.IN.HEADER;
import static pond.web.api.contract.ParameterObject.SCHEMA.*;

public class ContractRouter extends Router {

    protected String name;
    protected String title;
    protected String version;
    protected String summary;
    protected String description;

    protected List<String> produces = new ArrayList<>();
    protected List<String> consumes = new ArrayList<>();

    protected ContractRouter() {
    }



    public static ContractRouter of(Object dummy) {
        ContractRouter ret = new ContractRouter();
        Class contract = dummy.getClass();
        ret.name = contract.getName();
        var routePrefix = ret.basePath;
        var annos_type = contract.getDeclaredAnnotations();
        for (var tag : annos_type) {
            if (tag instanceof Contract.Title) {
                ret.title = ((Contract.Title) tag).value();
            }
            if (tag instanceof Contract.Consumes) {
                ret.consumes.addAll(Arrays.asList(((Contract.Consumes) tag).value()));
            }
            if (tag instanceof Contract.Produces) {
                ret.produces.addAll(Arrays.asList(((Contract.Produces) tag).value()));
            }
            if (tag instanceof Contract.Description) {
                ret.description = ((Contract.Description) tag).value();
            }
            if (tag instanceof Contract.Summary) {
                ret.summary = ((Contract.Summary) tag).value();
            }
            if (tag instanceof Contract.Version) {
                ret.version = ((Contract.Version) tag).value();
            }
            if (tag instanceof Contract.RouteConfig.RoutePrefix) {
                var base = ret.basePath;
                if (!base.endsWith("/")) {
                    base = base + "/";
                }
                var prefx = ((Contract.RouteConfig.RoutePrefix) tag).value();
                if (prefx.startsWith("/")) {
                    prefx = prefx.substring(1);
                }
                routePrefix = base + prefx;
                if (!routePrefix.startsWith("/")) {
                    routePrefix = "/" + routePrefix;
                }
            }
        }

        // methods
        var methods = contract.getDeclaredMethods();
        next_method:
        for (var method : methods) {
            var anno_method = method.getDeclaredAnnotations();
            Set<HttpMethod> httpMethods = new HashSet<>();
            List<String> produces = new ArrayList<>() {{
            }};
            List<String> consumes = new ArrayList<>() {{
            }};
            String path = "/" + method.getName();
            ;
            Class<?> responseType = Object.class;

            //getEntry route path
            for (var tag : anno_method) {
                if (tag instanceof Contract.RouteConfig.Route) {
                    path = ((Contract.RouteConfig.Route) tag).value();
                    if (!path.startsWith("/")) path = "/" + path;
                    break;
                }
            }

            for (var tag : anno_method) {
                if (tag instanceof Contract.Ignore) {
                    continue next_method;
                }
                if (tag instanceof Contract.Produces) {
                    produces.addAll(Arrays.asList(((Contract.Produces) tag).value()));
                }
                if (tag instanceof Contract.Consumes) {
                    consumes.addAll(Arrays.asList(((Contract.Consumes) tag).value()));
                }
                if (tag instanceof Contract.RouteConfig.ResponseType) {
                    responseType = ((Contract.RouteConfig.ResponseType) tag).value();
                    //TODO refine this;
                    //produces.add();
                }

                if (tag instanceof Contract.RouteConfig.Methods.ALL) {
                    httpMethods.clear();
                    httpMethods.addAll(Set.of(
                            HttpMethod.HEAD,
                            HttpMethod.GET,
                            HttpMethod.POST,
                            HttpMethod.PUT,
                            HttpMethod.DELETE,
                            HttpMethod.OPTIONS,
                            HttpMethod.TRACE,
                            HttpMethod.CONNECT
                    ));
                } else if (tag instanceof Contract.RouteConfig.Methods.DEFAULT) {
                    httpMethods.clear();
                    httpMethods.addAll(Set.of(
                            HttpMethod.HEAD,
                            HttpMethod.GET,
                            HttpMethod.POST
                    ));
                } else if (tag instanceof Contract.RouteConfig.Methods.GET) {
                    httpMethods.add(HttpMethod.GET);
                } else if (tag instanceof Contract.RouteConfig.Methods.POST) {
                    httpMethods.add(HttpMethod.POST);
                } else if (tag instanceof Contract.RouteConfig.Methods.PUT) {
                    httpMethods.add(HttpMethod.PUT);
                } else if (tag instanceof Contract.RouteConfig.Methods.DELETE) {
                    httpMethods.add(HttpMethod.DELETE);
                } else if (tag instanceof Contract.RouteConfig.Methods.TRACE) {
                    httpMethods.add(HttpMethod.TRACE);
                } else if (tag instanceof Contract.RouteConfig.Methods.OPTIONS) {
                    httpMethods.add(HttpMethod.CONNECT);
                } else if (tag instanceof Contract.RouteConfig.Methods.HEAD) {
                    httpMethods.add(HttpMethod.HEAD);
                } else if (tag instanceof Contract.RouteConfig.Methods.CONNECT) {
                    httpMethods.add(HttpMethod.CONNECT);
                }
            }

            //building CtxHandler for method
            //set defaults
            if (httpMethods.size() == 0) {
                httpMethods.addAll(Set.of(
                        HttpMethod.HEAD,
                        HttpMethod.GET,
                        HttpMethod.POST
                ));
            }
            Integer mask = HttpMethod.mask(S._for(httpMethods).joinArray(new HttpMethod[0]));

            var method_params_result = new ArrayList<Tuple.T7<
                    Integer, String, Class<?>,
                                                                 ParameterObject.IN,
                                                                 ParameterObject.SCHEMA,
                    Annotation, Boolean>>();

            //parameterObjects
            var method_params = method.getParameters();
            for (var param_def : method_params) {
                String name;
                name = param_def.getName();
                for (var tag : anno_method) {
                    if (tag instanceof Contract.Parameters.Name) {
                        name = ((Contract.Parameters.Name) tag).value();
                        break;
                    }
                }
                boolean required = false;
                for (var tag : anno_method) {
                    if (tag instanceof Contract.Parameters.Required) {
                        required = true;
                        break;
                    }
                }

                S.echo("param_def.getName", name);
                var type = param_def.getType();
                S.echo("param_def.getType", type);
                //shortcuts
                if (type.equals(Request.class)) {
                    method_params_result.add(pair(
                            mask, name, type, CTX, REQ, null, required
                    ));
                    continue;
                }
                if (type.equals(Response.class)) {
                    method_params_result.add(pair(
                            mask, name, type, CTX, RESP, null, required
                    ));
                    continue;
                }
                //TODO generalise IN & SCHEMA
                //TODO Ctx? manually bind?

                var anno_param = param_def.getAnnotations();

                ParameterObject.IN in = null;
                ParameterObject.SCHEMA schema = null;
                Annotation annotation = null;

                for (var tag : anno_method) {
                    if (tag instanceof ParameterInContract.Path) {
                        in = ParameterObject.IN.PATH;
                        break;
                    }
                    if (tag instanceof ParameterInContract.Query) {
                        in = ParameterObject.IN.QUERIES;
                        break;
                    }
                    if (tag instanceof ParameterInContract.Header) {
                        in = ParameterObject.IN.HEADER;
                        break;
                    }
                    if (tag instanceof ParameterInContract.BodyForm) {
                        in = ParameterObject.IN.BODY_FORM;
                        break;
                    }
                    if (tag instanceof ParameterInContract.BodyJson) {
                        in = ParameterObject.IN.BODY_JSON;
                        break;
                    }
                    if (tag instanceof ParameterInContract.Body) {
                        in = ParameterObject.IN.BODY_RAW;
                        break;
                    }
                    if (tag instanceof ParameterInContract.BodyXml) {
                        in = ParameterObject.IN.BODY_XML;
                        break;
                    }
                    if (tag instanceof ParameterInContract.Cookie) {
                        in = ParameterObject.IN.COOKIE;
                        annotation = tag;
                        break;
                    }
                }

                if (type.isPrimitive() || S._is_wrapper_type(type)) {
                    if (type.equals(Integer.class) || type.equals(Integer.TYPE)) {
                        schema = ParameterObject.SCHEMA.INT;
                    } else if (type.equals(Double.class) || type.equals(Double.TYPE)) {
                        schema = ParameterObject.SCHEMA.NUMBER;
                    } else if (type.equals(Float.class) || type.equals(Float.TYPE)) {
                        schema = ParameterObject.SCHEMA.NUMBER;
                    } else if (type.equals(Boolean.class) || type.equals(Boolean.TYPE)) {
                        schema = ParameterObject.SCHEMA.BOOL;
                    } else throw new RuntimeException("UnSupported primitive type" + type);
                } else if (type.isAssignableFrom(Date.class)) {
                    for (var tag : anno_method) {
                        if (tag instanceof ParameterSchemaContract.LongToDate) {
                            schema = ParameterObject.SCHEMA.LONG_DATE;
                            break;
                        }
                        if (tag instanceof ParameterSchemaContract.StringToDate) {
                            schema = ParameterObject.SCHEMA.STR_DATE;
                            annotation = tag;
                            break;
                        }
                    }
                    throw new RuntimeException("ParameterObject is declared as Date but no annotations provided. use LongToDate or StringToDate");
                } else if (type.isAssignableFrom(String.class)) {
                    schema = ParameterObject.SCHEMA.STR;
                } else {
                    schema = ParameterObject.SCHEMA.ANY;
                }

                if (in == null) in = ParameterObject.IN.ANY;
                if (schema == null) schema = ParameterObject.SCHEMA.ANY;
                method_params_result.add(pair(mask, name, type, in, schema, annotation, required));
            }

            //build pre-handlers
            //consumer ---> converter/validators ---> stack push ---> dynamic invoked proccser --> produces
            List<Entry> entries = new ArrayList<>();
            List<Tuple<Entry, Function<Object, HttpCtx>>> providers = new ArrayList<>();
            List<Tuple<Entry, Function.F2<Object, String, Object>>> validators = new ArrayList<>();
            CtxHandler<HttpCtx> processor = null;

            //TODO http-prioritance-ordinal

            S._for(method_params_result).map(t -> {
                String name = t._b;
                Class type = t._c;
                ParameterObject.IN in = t._d;
                ParameterObject.SCHEMA schema = t._e;
                Annotation a = t._f;

                Function<Object, HttpCtx> valueProvider;

                //built-in
                if (Ctx.class.isAssignableFrom(type)) {
                    return pair(Ctx.SELF, null, name, type, schema, a, t._g);
                } else if (Request.class.isAssignableFrom(type)) {
                    return pair(HttpCtx.REQ, null, name, type, schema, a, t._g);
                } else if (Response.class.isAssignableFrom(type)) {
                    return pair(HttpCtx.RESP, null, name, type, schema, a, t._g);
                }

                if (in == CTX) {
                    if (schema == REQ) {
                        return pair(HttpCtx.REQ, null, name, type, schema, a, t._g);
                    } else if (schema == RESP) {
                        return pair(HttpCtx.RESP, null, name, type, schema, a, t._g);
                    } else if (schema == ParameterObject.SCHEMA.CTX) {
                        return pair(Ctx.SELF, null, name, type, schema, a, t._g);
                    } else return pair(new Entry(type, name), null, name, type, schema, a, t._g);
                }

                if (in == ParameterObject.IN.PATH) {
                    valueProvider = ctx -> S._for((((HttpCtx.Queries) ctx::bind).inUrlParams().get(name))).first();
                } else if (in == ParameterObject.IN.QUERIES) {
                    valueProvider = ctx -> (((HttpCtx.Queries) ctx::bind).query(name));
                } else if (in == ParameterObject.IN.COOKIE) {
                    var CookieName = ((ParameterInContract.Cookie) a).value();
                    valueProvider = ctx -> Optional.of(((HttpCtx.Cookies) ctx::bind).cookie(CookieName))
                            .map(Cookie::value).orElse(null);
                } else if (in == HEADER) {
                    valueProvider = ctx -> S._for((((HttpCtx.Headers) ctx::bind).headers().get(name))).first();
                } else if (in == ParameterObject.IN.BODY_FORM) {
                    valueProvider = ctx -> {
                        var c = (HttpCtx.Body) ctx::bind;
                        try {
                            return S._for(c.bodyAsMultipart().attrs().get(name)).first();
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    };
                } else if (in == ParameterObject.IN.BODY_XML) {
                    //TODO
                    throw new RuntimeException("BODY_XML is not supported yet");
                } else if (in == ParameterObject.IN.BODY_JSON) {
                    valueProvider = ctx -> {
                        var c = (HttpCtx.Body) ctx::bind;
                        return Optional.of(c.bodyAsJson()).map(m -> m.get(name)).orElse(null);
                    };
                } else if (in == ParameterObject.IN.BODY_RAW) {
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

                Entry canonicalEntry = new Entry(name + ":" + type.getCanonicalName());
                return Tuple.t7(canonicalEntry, valueProvider, name, type, schema, a, t._g);
            }).map(tt -> {
                Entry entry = tt._a;
                //Function<Object, HttpCtx> provider = tt._b;
                String name = tt._c;
                Class type = tt._d;
                ParameterObject.SCHEMA schema = tt._e;
                Annotation a = tt._f;
                boolean required = tt._g;
                //data validation and conversion here


                Function.F2<Object, String, Object> convertFunc = (n, any) -> any;

                if (type.isPrimitive() || S._is_wrapper_type(type)) {
                    S.echo("primitive validators", type);
                    required = true;
                    if (type.equals(Integer.class) || type.equals(Integer.TYPE)) {
                        S._assert(schema == ParameterObject.SCHEMA.INT, "Schema defined as INT, but actually " + type);
                        convertFunc = (n, any) -> Convert.toInt(S.avoidNull(any, "0"));
                    } else if (type.equals(Double.class) || type.equals(Double.TYPE)) {
                        S._assert(schema == ParameterObject.SCHEMA.NUMBER, "Schema defined as NUMBER, but actually " + type);
                        convertFunc = (n, any) -> Convert.toDouble(S.avoidNull(any, "0"));
                    } else if (type.equals(Float.class) || type.equals(Float.TYPE)) {
                        S._assert(schema == ParameterObject.SCHEMA.NUMBER, "Schema defined as NUMBER, but actually " + type);
                        convertFunc = (n, any) -> Convert.toDouble(S.avoidNull(any, "0"));
                    } else if (type.equals(Boolean.class) || type.equals(Boolean.TYPE)) {
                        S._assert(schema == ParameterObject.SCHEMA.BOOL, "Schema defined as NUMBER, but actually " + type);
                        convertFunc = (n, any) -> Convert.toBoolean(any, false);
                    } else throw new RuntimeException("UnSupported primitive type" + type);
                } else if (type.isAssignableFrom(Date.class)) {
                    assert a != null;
                    if (a instanceof ParameterSchemaContract.LongToDate && schema == LONG_DATE) {
                        convertFunc = (n, any) -> Convert.toDate((Long) any);
                    }
                    if (a instanceof ParameterSchemaContract.StringToDate && schema == STR_DATE) {
                        convertFunc = (n, any) -> S._try_ret(() -> Convert.toDate(String.valueOf(any), ((ParameterSchemaContract.StringToDate) a).value()));
                    }
                }

                //required
                Function.F2<Object, String, Object> requireFunc = (n, any) -> {
                    if (any == null) {
                        throw new EndToEndException(400, n + " is required");
                    } else return null;
                };

                if (required) {
                    //compose requireFunc
                    final Function.F2<Object, String, Object> finalConverter = convertFunc;
                    convertFunc = (n, any) -> {
                        requireFunc.apply(n, any);
                        return finalConverter.apply(n, any);
                    };
                }
                return pair(entry, tt._b, convertFunc, required, name);
            }).each(ttt -> {
                entries.add(ttt._a);
                if (ttt._b != null) {
                    providers.add(pair(ttt._a, (Function<Object, HttpCtx>) ttt._b));
                    validators.add(pair(ttt._a, ttt._c));
                }
            });

            //build Handlers

            List<CtxHandler> finalHandlers = new ArrayList<>();

            CtxHandler<HttpCtx> consumer = http -> {
                var ctx = (RouterCtx) http::bind;
                var pass = consumes.size() == 0 ||
                        S._for(consumes).some(
                                incommingContentType ->
                                        Optional.ofNullable(ctx.request().headers().get(HttpHeaderNames.CONTENT_TYPE))
                                                .flatMap(ct -> Optional.of(incommingContentType.equalsIgnoreCase(ct)))
                                                .orElse(false)

                        );
                if (!pass) ctx.continueRouting();
            };
            finalHandlers.add(consumer);
            //providers
            finalHandlers.addAll(S._for(providers).map(p -> {
                Entry<Object> entry = p._a;
                Function<Object, HttpCtx> provider = p._b;
                return CtxHandler.process(Ctx.SELF, ctx -> provider.apply(ctx::bind), entry);
            }).toList());

            //validators
            finalHandlers.addAll(S._for(validators).map(v -> {
                Entry<Object> entry = v._a;
                Function.F2<Object, String, Object> validator = v._b;
                return CtxHandler.process(
                        entry,
                        any -> {
                            try {
                                return validator.apply(entry.name(), any);
                            } catch (RuntimeException e) {
                                throw new EndToEndException(400, e.getClass() + " " + e.getMessage());
                            }
                        },
                        entry
                );
            }).toList());

            //dynamic invoker

            CtxHandler invoker = CtxHandler.process(
                    Ctx.SELF, ctx -> {
                        try {
                            Object[] params = S._for(entries).map(entry -> ctx.getEntry(entry)).joinArray(new Object[entries.size()]);
                            S.echo("BEFORE EXECUTAION", entries, params);
                            return method.invoke(dummy, params);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                            throw new EndToEndException(500, e.getMessage());
                        }
                    }
            );
            finalHandlers.add(invoker);

            //

            CtxHandler<HttpCtx> producer = http -> {
                var ctx = (RouterCtx & HttpCtx.Send) http::bind;
                var headers = ctx.response().headers();
                var contentType = headers.get(HttpHeaderNames.CONTENT_TYPE);
                S._for(produces).each(outgoingContentType -> {
                    if (contentType == null || contentType.contains(outgoingContentType)) {
                        headers.add(HttpHeaderNames.CONTENT_TYPE, outgoingContentType);
                    }
                });
                var result = http.getEntry(Ctx.LAST_RESULT);
                S.echo("Show me", result);
                ctx.send(result);
            };
            finalHandlers.add(producer);

            var finalPath = RouterAPI.sanitisePath(routePrefix.endsWith("/")
                    ? routePrefix + path
                    : routePrefix + "/" + path);

            S.echo("Adding Contract", mask, finalPath, finalHandlers);
            ret.use(mask, finalPath, finalHandlers.toArray(new CtxHandler[0]));
        }


        return ret;
    }
}
