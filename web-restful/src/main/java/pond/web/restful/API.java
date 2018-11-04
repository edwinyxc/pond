package pond.web.restful;

import pond.common.S;
import pond.common.f.Callback;
import pond.common.f.Tuple;
import pond.core.CtxHandler;
import pond.web.*;
import pond.web.http.HttpCtx;
import pond.web.router.Route;
import pond.web.router.Router;

import java.util.*;

/**
 * Created by edwin on 3/12/2017.
 */
public class API extends Router {

    public final String title;
    public final String desc;

    List<String> tags;
    public String version = "1.0.0";

    final private Map<String, Path> paths = new LinkedHashMap<>();

    public API() {
        this.title = this.getClass().getName();
        this.desc = this.title;
    }

    public String basePath() {
        return super.basePath;
    }

    private List<Parameter> implodeParaDefStruct(ParamDefStruct def) {
        List<Parameter> ret = new ArrayList<>();
        List<ParamDef> defs = def.defs();
        for(ParamDef d : defs) {
           if(d instanceof ParamDefStruct) {
               ret.addAll(implodeParaDefStruct((ParamDefStruct) d));
           } else {
               Parameter p = parameter(d);
               if(p != null) ret.add(parameter(d));
           }
        }
        return ret;
    }

    private Parameter parameter(ParamDef def) {
        if (def.in == ParamDef.ParamIn.COMPOSED) {
            //ignore
            return null;
        }

        Parameter ret = new Parameter()
                .description(def.desc)
                .name(def.name)
                .in(S.avoidNull(def.in, ParamDef.ParamIn.FORM_DATA))
                .type(S.avoidNull(def.type, ParamDef.ParamType.STRING))
                .schema(def.schema)
                ;


        if (def.in == ParamDef.ParamIn.BODY
                && def instanceof ParamDefStruct) {
            ret.schema(((ParamDefStruct) def).schema());
            if (!def.required) ret.allowEmptyValue();
        }

        ret.required(def.required);

        //TODO array & format

        //TODO items http://swagger.io/specification/#securityRequirementObject

        //TODO collectionFormat

        return ret;
    }

    private List<Parameter> parameters(List<ParamDef> defs) {
        List<Parameter> ret = new ArrayList<>();
        for(ParamDef def : defs) {
            if(def instanceof ParamDefStruct) {
                ret.addAll(implodeParaDefStruct((ParamDefStruct) def));
            } else {
                Parameter parameter = parameter(def);
                if(parameter != null) ret.add(parameter);
            }
        }
        return ret;
    }

    Tuple<Integer, Operation.Response> response(ResultDef def) {
        Operation.Response ret = new Operation.Response(def.description);
        //TODO header..

        Map<String, Operation.Response.Header> headers = new HashMap<>();

        for (Object header : def.headers) {
            headers.put((String) header, new Operation.Response.Header((String) header, "string"));
        }

        if (def.schema != null)
            ret.schema(def.schema);

        return Tuple.pair(def.httpStatusCode, ret);
    }

    void install(Map<String, Path> paths, APIHandler def, Route route) {

        String abs_path = buildPathForRoute(route.basePath());
        //find path
        Path path = paths.computeIfAbsent(abs_path, Path::new);

        //if the current ctx-Handler is not the last handler, treat def as a filter-handler
        if (route.handlers().indexOf(def) != route.handlers().size() - 1) {
            List<Parameter> parameters_path = path.parameters();
            for (ParamDef paramDef : def.paramDefs) {
                // if no such identical (name & in) in path's parameters
                if (S._for(parameters_path).every(p -> !(p.name().equals(paramDef.name)) && p.in().equals(paramDef.in.val))) {
                    //add paramDef as a new Path parameter
                    parameters_path.addAll(parameters(Collections.singletonList(paramDef)));
                }
            }
//            S.echo("New Path parameters Added @ " + path.path, path.parameters(), path.parameters() == parameters_path);
        } else {

            //find existing operation
            Operation operation = path.method(route.method);

            if (operation == null) {
                //build Operation
                operation = new Operation()
                        .operationId(route.method + " " + abs_path)
                        .description(route.method.toString());

                operation.consumes(new HashSet<>());
                operation.produces(new HashSet<>());

                path.method(route.method, operation);
            }

            Set<String> consumes = operation.consumes();
            Set<String> produces = operation.produces();

            operation.parameters( parameters(def.paramDefs));
//                        S._for(def.paramDefs).map(p -> {
//                            consumes.addAll(Arrays.asList(p.consumes));
//                            return parameters(Collections.singletonList(p));
//                        }).compact().toList()
//                );

            operation.responses(
                    S._for(def.resultDefs).map(r -> {
                        produces.addAll(r.produces);
                        return response(r);
                    }).compact().toList()
            );

            operation.consumes(consumes);
            operation.produces(produces);
            path.method(route.method, operation);
        }

    }

    //TODO why we need this? try to remove
    private String santiliseNewPath(String base, String path) {
        // Path root is "" rather than "/", so we need to make sure path length > 0
        String newPath = (base.endsWith("/") && path.length() > 0)
                ? (base.substring(0, base.length() - 1) + path)
                : (base + path);
        if (newPath.equals("")) newPath = "/";
        return newPath;
    }

    public Map<String, Path> getAllPathsRecursively() {
        Map<String, Path> ret = new LinkedHashMap<>();
        String base = this.absolutePath();
//        S.echo("++++");
//        S.echo("BasePath", base);
//        S.echo("hasChildren", children.size() > 0);
//        S.echo("Paths", S._for(paths.entrySet()).map(Map.Entry::getKey).toList(), paths.size());


        S._for(paths.entrySet()).each(entry -> {
            Path path = entry.getValue();
            ret.put(santiliseNewPath(base, path.path), path);
        });

        S._for(children).each(child -> {
            if (child instanceof API) {
//                S.echo("child_base", ((API) child).basePath());

                Path path_this = ret.get(santiliseNewPath(((API) child).basePath(), "/"));
//                if (path_this != null)
//                    S.echo("child_base_path", path_this.path, path_this.parameters());
//                else
//                    S.echo("child_base_path", "Null");

                for (Map.Entry<String, Path> entry : ((API) child).getAllPathsRecursively().entrySet()) {
                    String pathStr = entry.getKey();
                    Path path_child = entry.getValue();
                    if (path_this != null && path_this.parameters().size() > 0) {

//                        S.echo("Found path queries @ " + pathStr, path_this.parameters());

                        List<Parameter> parameters_path_child = path_child.parameters();
                        for (Parameter parameter : path_this.parameters()) {
                            // if no such identical (name & in) in path's parameters
                            if (S._for(parameters_path_child).every(p ->
                                    !(p.name().equals(parameter.name())) && p.in().equals(parameter.in()))) {
                                //add paramDef as a new Path parameter
                                parameters_path_child.add(parameter);
                            }
                        }
                    }
                    ret.put(pathStr, path_child);
                }
            }
        });

//        S.echo("----");
        return ret;
    }

    @Override
    public void configRoute(Route route, CtxHandler handler) {
        super.configRoute(route, handler);
        if (handler instanceof APIHandler) {
            APIHandler def = (APIHandler) handler;
            install(paths, def, route);
        }
    }

//    /*Def schemas */
//    public API def(String name, Schema schema) {
//
//    }

    /*Generated def routers */

    public static APIHandler def(Callback<HttpCtx> handler) {
        return new APIHandler(S.array(), S.array(), handler::apply);
    }

    public static <R1> APIHandler def(ResultDef<R1> ret_1, Callback.C2<HttpCtx, ResultDef<R1>> handler) {
        return new APIHandler(S.array(), S.array(ret_1), ctx -> handler.apply(ctx, ret_1));
    }

    public static <R1, R2> APIHandler def(ResultDef<R1> ret_1, ResultDef<R2> ret_2, Callback.C3<HttpCtx, ResultDef<R1>, ResultDef<R2>> handler) {
        return new APIHandler(S.array(), S.array(ret_1, ret_2), ctx -> handler.apply(ctx, ret_1, ret_2));
    }

    public static <R1, R2, R3> APIHandler def(ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, Callback.C4<HttpCtx, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>> handler) {
        return new APIHandler(S.array(), S.array(ret_1, ret_2, ret_3), ctx -> handler.apply(ctx, ret_1, ret_2, ret_3));
    }

    public static <R1, R2, R3, R4> APIHandler def(ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, Callback.C5<HttpCtx, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>> handler) {
        return new APIHandler(S.array(), S.array(ret_1, ret_2, ret_3, ret_4), ctx -> handler.apply(ctx, ret_1, ret_2, ret_3, ret_4));
    }

    public static <R1, R2, R3, R4, R5> APIHandler def(ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, Callback.C6<HttpCtx, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>> handler) {
        return new APIHandler(S.array(), S.array(ret_1, ret_2, ret_3, ret_4, ret_5), ctx -> handler.apply(ctx, ret_1, ret_2, ret_3, ret_4, ret_5));
    }

    public static <R1, R2, R3, R4, R5, R6> APIHandler def(ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, Callback.C7<HttpCtx, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>> handler) {
        return new APIHandler(S.array(), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6), ctx -> handler.apply(ctx, ret_1, ret_2, ret_3, ret_4, ret_5, ret_6));
    }

    public static <R1, R2, R3, R4, R5, R6, R7> APIHandler def(ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, Callback.C8<HttpCtx, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>> handler) {
        return new APIHandler(S.array(), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7), ctx -> handler.apply(ctx, ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7));
    }

    public static <R1, R2, R3, R4, R5, R6, R7, R8> APIHandler def(ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, ResultDef<R8> ret_8, Callback.C9<HttpCtx, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>, ResultDef<R8>> handler) {
        return new APIHandler(S.array(), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8), ctx -> handler.apply(ctx, ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8));
    }

    public static <P1> APIHandler def(ParamDef<P1> def_1, Callback.C2<HttpCtx, P1> handler) {
        return new APIHandler(S.array(def_1), S.array(), ctx -> handler.apply(ctx, def_1.get(ctx)));
    }

    public static <P1, R1> APIHandler def(ParamDef<P1> def_1, ResultDef<R1> ret_1, Callback.C3<HttpCtx, P1, ResultDef<R1>> handler) {
        return new APIHandler(S.array(def_1), S.array(ret_1), ctx -> handler.apply(ctx, def_1.get(ctx), ret_1));
    }

    public static <P1, R1, R2> APIHandler def(ParamDef<P1> def_1, ResultDef<R1> ret_1, ResultDef<R2> ret_2, Callback.C4<HttpCtx, P1, ResultDef<R1>, ResultDef<R2>> handler) {
        return new APIHandler(S.array(def_1), S.array(ret_1, ret_2), ctx -> handler.apply(ctx, def_1.get(ctx), ret_1, ret_2));
    }

    public static <P1, R1, R2, R3> APIHandler def(ParamDef<P1> def_1, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, Callback.C5<HttpCtx, P1, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>> handler) {
        return new APIHandler(S.array(def_1), S.array(ret_1, ret_2, ret_3), ctx -> handler.apply(ctx, def_1.get(ctx), ret_1, ret_2, ret_3));
    }

    public static <P1, R1, R2, R3, R4> APIHandler def(ParamDef<P1> def_1, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, Callback.C6<HttpCtx, P1, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>> handler) {
        return new APIHandler(S.array(def_1), S.array(ret_1, ret_2, ret_3, ret_4), ctx -> handler.apply(ctx, def_1.get(ctx), ret_1, ret_2, ret_3, ret_4));
    }

    public static <P1, R1, R2, R3, R4, R5> APIHandler def(ParamDef<P1> def_1, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, Callback.C7<HttpCtx, P1, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>> handler) {
        return new APIHandler(S.array(def_1), S.array(ret_1, ret_2, ret_3, ret_4, ret_5), ctx -> handler.apply(ctx, def_1.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5));
    }

    public static <P1, R1, R2, R3, R4, R5, R6> APIHandler def(ParamDef<P1> def_1, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, Callback.C8<HttpCtx, P1, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>> handler) {
        return new APIHandler(S.array(def_1), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6), ctx -> handler.apply(ctx, def_1.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6));
    }

    public static <P1, R1, R2, R3, R4, R5, R6, R7> APIHandler def(ParamDef<P1> def_1, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, Callback.C9<HttpCtx, P1, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>> handler) {
        return new APIHandler(S.array(def_1), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7), ctx -> handler.apply(ctx, def_1.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7));
    }

    public static <P1, R1, R2, R3, R4, R5, R6, R7, R8> APIHandler def(ParamDef<P1> def_1, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, ResultDef<R8> ret_8, Callback.C10<HttpCtx, P1, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>, ResultDef<R8>> handler) {
        return new APIHandler(S.array(def_1), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8), ctx -> handler.apply(ctx, def_1.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8));
    }

    public static <P1, P2> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, Callback.C3<HttpCtx, P1, P2> handler) {
        return new APIHandler(S.array(def_1, def_2), S.array(), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx)));
    }

    public static <P1, P2, R1> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ResultDef<R1> ret_1, Callback.C4<HttpCtx, P1, P2, ResultDef<R1>> handler) {
        return new APIHandler(S.array(def_1, def_2), S.array(ret_1), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), ret_1));
    }

    public static <P1, P2, R1, R2> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ResultDef<R1> ret_1, ResultDef<R2> ret_2, Callback.C5<HttpCtx, P1, P2, ResultDef<R1>, ResultDef<R2>> handler) {
        return new APIHandler(S.array(def_1, def_2), S.array(ret_1, ret_2), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), ret_1, ret_2));
    }

    public static <P1, P2, R1, R2, R3> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, Callback.C6<HttpCtx, P1, P2, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>> handler) {
        return new APIHandler(S.array(def_1, def_2), S.array(ret_1, ret_2, ret_3), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), ret_1, ret_2, ret_3));
    }

    public static <P1, P2, R1, R2, R3, R4> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, Callback.C7<HttpCtx, P1, P2, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>> handler) {
        return new APIHandler(S.array(def_1, def_2), S.array(ret_1, ret_2, ret_3, ret_4), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), ret_1, ret_2, ret_3, ret_4));
    }

    public static <P1, P2, R1, R2, R3, R4, R5> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, Callback.C8<HttpCtx, P1, P2, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>> handler) {
        return new APIHandler(S.array(def_1, def_2), S.array(ret_1, ret_2, ret_3, ret_4, ret_5), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5));
    }

    public static <P1, P2, R1, R2, R3, R4, R5, R6> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, Callback.C9<HttpCtx, P1, P2, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>> handler) {
        return new APIHandler(S.array(def_1, def_2), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6));
    }

    public static <P1, P2, R1, R2, R3, R4, R5, R6, R7> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, Callback.C10<HttpCtx, P1, P2, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>> handler) {
        return new APIHandler(S.array(def_1, def_2), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7));
    }

    public static <P1, P2, R1, R2, R3, R4, R5, R6, R7, R8> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, ResultDef<R8> ret_8, Callback.C11<HttpCtx, P1, P2, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>, ResultDef<R8>> handler) {
        return new APIHandler(S.array(def_1, def_2), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8));
    }

    public static <P1, P2, P3> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, Callback.C4<HttpCtx, P1, P2, P3> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3), S.array(), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx)));
    }

    public static <P1, P2, P3, R1> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ResultDef<R1> ret_1, Callback.C5<HttpCtx, P1, P2, P3, ResultDef<R1>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3), S.array(ret_1), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), ret_1));
    }

    public static <P1, P2, P3, R1, R2> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ResultDef<R1> ret_1, ResultDef<R2> ret_2, Callback.C6<HttpCtx, P1, P2, P3, ResultDef<R1>, ResultDef<R2>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3), S.array(ret_1, ret_2), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), ret_1, ret_2));
    }

    public static <P1, P2, P3, R1, R2, R3> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, Callback.C7<HttpCtx, P1, P2, P3, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3), S.array(ret_1, ret_2, ret_3), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), ret_1, ret_2, ret_3));
    }

    public static <P1, P2, P3, R1, R2, R3, R4> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, Callback.C8<HttpCtx, P1, P2, P3, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3), S.array(ret_1, ret_2, ret_3, ret_4), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), ret_1, ret_2, ret_3, ret_4));
    }

    public static <P1, P2, P3, R1, R2, R3, R4, R5> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, Callback.C9<HttpCtx, P1, P2, P3, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3), S.array(ret_1, ret_2, ret_3, ret_4, ret_5), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5));
    }

    public static <P1, P2, P3, R1, R2, R3, R4, R5, R6> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, Callback.C10<HttpCtx, P1, P2, P3, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6));
    }

    public static <P1, P2, P3, R1, R2, R3, R4, R5, R6, R7> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, Callback.C11<HttpCtx, P1, P2, P3, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7));
    }

    public static <P1, P2, P3, R1, R2, R3, R4, R5, R6, R7, R8> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, ResultDef<R8> ret_8, Callback.C12<HttpCtx, P1, P2, P3, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>, ResultDef<R8>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8));
    }

    public static <P1, P2, P3, P4> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, Callback.C5<HttpCtx, P1, P2, P3, P4> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4), S.array(), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx)));
    }

    public static <P1, P2, P3, P4, R1> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ResultDef<R1> ret_1, Callback.C6<HttpCtx, P1, P2, P3, P4, ResultDef<R1>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4), S.array(ret_1), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), ret_1));
    }

    public static <P1, P2, P3, P4, R1, R2> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ResultDef<R1> ret_1, ResultDef<R2> ret_2, Callback.C7<HttpCtx, P1, P2, P3, P4, ResultDef<R1>, ResultDef<R2>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4), S.array(ret_1, ret_2), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), ret_1, ret_2));
    }

    public static <P1, P2, P3, P4, R1, R2, R3> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, Callback.C8<HttpCtx, P1, P2, P3, P4, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4), S.array(ret_1, ret_2, ret_3), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), ret_1, ret_2, ret_3));
    }

    public static <P1, P2, P3, P4, R1, R2, R3, R4> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, Callback.C9<HttpCtx, P1, P2, P3, P4, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4), S.array(ret_1, ret_2, ret_3, ret_4), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), ret_1, ret_2, ret_3, ret_4));
    }

    public static <P1, P2, P3, P4, R1, R2, R3, R4, R5> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, Callback.C10<HttpCtx, P1, P2, P3, P4, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4), S.array(ret_1, ret_2, ret_3, ret_4, ret_5), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5));
    }

    public static <P1, P2, P3, P4, R1, R2, R3, R4, R5, R6> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, Callback.C11<HttpCtx, P1, P2, P3, P4, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6));
    }

    public static <P1, P2, P3, P4, R1, R2, R3, R4, R5, R6, R7> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, Callback.C12<HttpCtx, P1, P2, P3, P4, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7));
    }

    public static <P1, P2, P3, P4, R1, R2, R3, R4, R5, R6, R7, R8> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, ResultDef<R8> ret_8, Callback.C13<HttpCtx, P1, P2, P3, P4, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>, ResultDef<R8>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8));
    }

    public static <P1, P2, P3, P4, P5> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, Callback.C6<HttpCtx, P1, P2, P3, P4, P5> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5), S.array(), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx)));
    }

    public static <P1, P2, P3, P4, P5, R1> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ResultDef<R1> ret_1, Callback.C7<HttpCtx, P1, P2, P3, P4, P5, ResultDef<R1>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5), S.array(ret_1), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), ret_1));
    }

    public static <P1, P2, P3, P4, P5, R1, R2> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ResultDef<R1> ret_1, ResultDef<R2> ret_2, Callback.C8<HttpCtx, P1, P2, P3, P4, P5, ResultDef<R1>, ResultDef<R2>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5), S.array(ret_1, ret_2), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), ret_1, ret_2));
    }

    public static <P1, P2, P3, P4, P5, R1, R2, R3> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, Callback.C9<HttpCtx, P1, P2, P3, P4, P5, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5), S.array(ret_1, ret_2, ret_3), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), ret_1, ret_2, ret_3));
    }

    public static <P1, P2, P3, P4, P5, R1, R2, R3, R4> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, Callback.C10<HttpCtx, P1, P2, P3, P4, P5, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5), S.array(ret_1, ret_2, ret_3, ret_4), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), ret_1, ret_2, ret_3, ret_4));
    }

    public static <P1, P2, P3, P4, P5, R1, R2, R3, R4, R5> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, Callback.C11<HttpCtx, P1, P2, P3, P4, P5, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5), S.array(ret_1, ret_2, ret_3, ret_4, ret_5), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5));
    }

    public static <P1, P2, P3, P4, P5, R1, R2, R3, R4, R5, R6> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, Callback.C12<HttpCtx, P1, P2, P3, P4, P5, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6));
    }

    public static <P1, P2, P3, P4, P5, R1, R2, R3, R4, R5, R6, R7> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, Callback.C13<HttpCtx, P1, P2, P3, P4, P5, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7));
    }

    public static <P1, P2, P3, P4, P5, R1, R2, R3, R4, R5, R6, R7, R8> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, ResultDef<R8> ret_8, Callback.C14<HttpCtx, P1, P2, P3, P4, P5, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>, ResultDef<R8>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8));
    }

    public static <P1, P2, P3, P4, P5, P6> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, Callback.C7<HttpCtx, P1, P2, P3, P4, P5, P6> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6), S.array(), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx)));
    }

    public static <P1, P2, P3, P4, P5, P6, R1> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ResultDef<R1> ret_1, Callback.C8<HttpCtx, P1, P2, P3, P4, P5, P6, ResultDef<R1>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6), S.array(ret_1), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), ret_1));
    }

    public static <P1, P2, P3, P4, P5, P6, R1, R2> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ResultDef<R1> ret_1, ResultDef<R2> ret_2, Callback.C9<HttpCtx, P1, P2, P3, P4, P5, P6, ResultDef<R1>, ResultDef<R2>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6), S.array(ret_1, ret_2), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), ret_1, ret_2));
    }

    public static <P1, P2, P3, P4, P5, P6, R1, R2, R3> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, Callback.C10<HttpCtx, P1, P2, P3, P4, P5, P6, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6), S.array(ret_1, ret_2, ret_3), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), ret_1, ret_2, ret_3));
    }

    public static <P1, P2, P3, P4, P5, P6, R1, R2, R3, R4> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, Callback.C11<HttpCtx, P1, P2, P3, P4, P5, P6, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6), S.array(ret_1, ret_2, ret_3, ret_4), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), ret_1, ret_2, ret_3, ret_4));
    }

    public static <P1, P2, P3, P4, P5, P6, R1, R2, R3, R4, R5> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, Callback.C12<HttpCtx, P1, P2, P3, P4, P5, P6, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6), S.array(ret_1, ret_2, ret_3, ret_4, ret_5), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5));
    }

    public static <P1, P2, P3, P4, P5, P6, R1, R2, R3, R4, R5, R6> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, Callback.C13<HttpCtx, P1, P2, P3, P4, P5, P6, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6));
    }

    public static <P1, P2, P3, P4, P5, P6, R1, R2, R3, R4, R5, R6, R7> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, Callback.C14<HttpCtx, P1, P2, P3, P4, P5, P6, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7));
    }

    public static <P1, P2, P3, P4, P5, P6, R1, R2, R3, R4, R5, R6, R7, R8> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, ResultDef<R8> ret_8, Callback.C15<HttpCtx, P1, P2, P3, P4, P5, P6, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>, ResultDef<R8>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8));
    }

    public static <P1, P2, P3, P4, P5, P6, P7> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, Callback.C8<HttpCtx, P1, P2, P3, P4, P5, P6, P7> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7), S.array(), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx)));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, R1> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ResultDef<R1> ret_1, Callback.C9<HttpCtx, P1, P2, P3, P4, P5, P6, P7, ResultDef<R1>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7), S.array(ret_1), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), ret_1));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, R1, R2> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ResultDef<R1> ret_1, ResultDef<R2> ret_2, Callback.C10<HttpCtx, P1, P2, P3, P4, P5, P6, P7, ResultDef<R1>, ResultDef<R2>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7), S.array(ret_1, ret_2), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), ret_1, ret_2));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, R1, R2, R3> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, Callback.C11<HttpCtx, P1, P2, P3, P4, P5, P6, P7, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7), S.array(ret_1, ret_2, ret_3), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), ret_1, ret_2, ret_3));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, R1, R2, R3, R4> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, Callback.C12<HttpCtx, P1, P2, P3, P4, P5, P6, P7, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7), S.array(ret_1, ret_2, ret_3, ret_4), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), ret_1, ret_2, ret_3, ret_4));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, R1, R2, R3, R4, R5> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, Callback.C13<HttpCtx, P1, P2, P3, P4, P5, P6, P7, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7), S.array(ret_1, ret_2, ret_3, ret_4, ret_5), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, R1, R2, R3, R4, R5, R6> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, Callback.C14<HttpCtx, P1, P2, P3, P4, P5, P6, P7, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, R1, R2, R3, R4, R5, R6, R7> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, Callback.C15<HttpCtx, P1, P2, P3, P4, P5, P6, P7, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, R1, R2, R3, R4, R5, R6, R7, R8> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, ResultDef<R8> ret_8, Callback.C16<HttpCtx, P1, P2, P3, P4, P5, P6, P7, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>, ResultDef<R8>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, Callback.C9<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8), S.array(), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx)));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, R1> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ResultDef<R1> ret_1, Callback.C10<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, ResultDef<R1>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8), S.array(ret_1), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), ret_1));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, R1, R2> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ResultDef<R1> ret_1, ResultDef<R2> ret_2, Callback.C11<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, ResultDef<R1>, ResultDef<R2>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8), S.array(ret_1, ret_2), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), ret_1, ret_2));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, R1, R2, R3> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, Callback.C12<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8), S.array(ret_1, ret_2, ret_3), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), ret_1, ret_2, ret_3));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, R1, R2, R3, R4> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, Callback.C13<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8), S.array(ret_1, ret_2, ret_3, ret_4), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), ret_1, ret_2, ret_3, ret_4));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, R1, R2, R3, R4, R5> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, Callback.C14<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8), S.array(ret_1, ret_2, ret_3, ret_4, ret_5), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, R1, R2, R3, R4, R5, R6> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, Callback.C15<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, R1, R2, R3, R4, R5, R6, R7> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, Callback.C16<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, R1, R2, R3, R4, R5, R6, R7, R8> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, ResultDef<R8> ret_8, Callback.C17<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>, ResultDef<R8>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, Callback.C10<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9), S.array(), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx)));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, R1> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ResultDef<R1> ret_1, Callback.C11<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, ResultDef<R1>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9), S.array(ret_1), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), ret_1));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, R1, R2> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ResultDef<R1> ret_1, ResultDef<R2> ret_2, Callback.C12<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, ResultDef<R1>, ResultDef<R2>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9), S.array(ret_1, ret_2), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), ret_1, ret_2));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, R1, R2, R3> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, Callback.C13<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9), S.array(ret_1, ret_2, ret_3), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), ret_1, ret_2, ret_3));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, R1, R2, R3, R4> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, Callback.C14<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9), S.array(ret_1, ret_2, ret_3, ret_4), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), ret_1, ret_2, ret_3, ret_4));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, R1, R2, R3, R4, R5> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, Callback.C15<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9), S.array(ret_1, ret_2, ret_3, ret_4, ret_5), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, R1, R2, R3, R4, R5, R6> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, Callback.C16<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, R1, R2, R3, R4, R5, R6, R7> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, Callback.C17<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, R1, R2, R3, R4, R5, R6, R7, R8> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, ResultDef<R8> ret_8, Callback.C18<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>, ResultDef<R8>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, Callback.C11<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10), S.array(), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx)));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, R1> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ResultDef<R1> ret_1, Callback.C12<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, ResultDef<R1>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10), S.array(ret_1), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), ret_1));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, R1, R2> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ResultDef<R1> ret_1, ResultDef<R2> ret_2, Callback.C13<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, ResultDef<R1>, ResultDef<R2>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10), S.array(ret_1, ret_2), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), ret_1, ret_2));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, R1, R2, R3> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, Callback.C14<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10), S.array(ret_1, ret_2, ret_3), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), ret_1, ret_2, ret_3));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, R1, R2, R3, R4> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, Callback.C15<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10), S.array(ret_1, ret_2, ret_3, ret_4), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), ret_1, ret_2, ret_3, ret_4));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, R1, R2, R3, R4, R5> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, Callback.C16<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10), S.array(ret_1, ret_2, ret_3, ret_4, ret_5), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, R1, R2, R3, R4, R5, R6> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, Callback.C17<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, R1, R2, R3, R4, R5, R6, R7> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, Callback.C18<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, R1, R2, R3, R4, R5, R6, R7, R8> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, ResultDef<R8> ret_8, Callback.C19<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>, ResultDef<R8>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, Callback.C12<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11), S.array(), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx)));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, R1> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ResultDef<R1> ret_1, Callback.C13<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, ResultDef<R1>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11), S.array(ret_1), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), ret_1));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, R1, R2> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ResultDef<R1> ret_1, ResultDef<R2> ret_2, Callback.C14<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, ResultDef<R1>, ResultDef<R2>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11), S.array(ret_1, ret_2), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), ret_1, ret_2));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, R1, R2, R3> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, Callback.C15<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11), S.array(ret_1, ret_2, ret_3), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), ret_1, ret_2, ret_3));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, R1, R2, R3, R4> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, Callback.C16<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11), S.array(ret_1, ret_2, ret_3, ret_4), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), ret_1, ret_2, ret_3, ret_4));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, R1, R2, R3, R4, R5> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, Callback.C17<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11), S.array(ret_1, ret_2, ret_3, ret_4, ret_5), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, R1, R2, R3, R4, R5, R6> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, Callback.C18<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, R1, R2, R3, R4, R5, R6, R7> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, Callback.C19<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, R1, R2, R3, R4, R5, R6, R7, R8> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, ResultDef<R8> ret_8, Callback.C20<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>, ResultDef<R8>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, Callback.C13<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12), S.array(), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx)));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, R1> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ResultDef<R1> ret_1, Callback.C14<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, ResultDef<R1>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12), S.array(ret_1), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), ret_1));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, R1, R2> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ResultDef<R1> ret_1, ResultDef<R2> ret_2, Callback.C15<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, ResultDef<R1>, ResultDef<R2>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12), S.array(ret_1, ret_2), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), ret_1, ret_2));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, R1, R2, R3> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, Callback.C16<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12), S.array(ret_1, ret_2, ret_3), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), ret_1, ret_2, ret_3));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, R1, R2, R3, R4> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, Callback.C17<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12), S.array(ret_1, ret_2, ret_3, ret_4), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), ret_1, ret_2, ret_3, ret_4));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, R1, R2, R3, R4, R5> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, Callback.C18<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12), S.array(ret_1, ret_2, ret_3, ret_4, ret_5), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, R1, R2, R3, R4, R5, R6> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, Callback.C19<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, R1, R2, R3, R4, R5, R6, R7> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, Callback.C20<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, R1, R2, R3, R4, R5, R6, R7, R8> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, ResultDef<R8> ret_8, Callback.C21<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>, ResultDef<R8>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, Callback.C14<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13), S.array(), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx)));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, R1> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ResultDef<R1> ret_1, Callback.C15<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, ResultDef<R1>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13), S.array(ret_1), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), ret_1));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, R1, R2> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ResultDef<R1> ret_1, ResultDef<R2> ret_2, Callback.C16<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, ResultDef<R1>, ResultDef<R2>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13), S.array(ret_1, ret_2), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), ret_1, ret_2));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, R1, R2, R3> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, Callback.C17<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13), S.array(ret_1, ret_2, ret_3), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), ret_1, ret_2, ret_3));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, R1, R2, R3, R4> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, Callback.C18<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13), S.array(ret_1, ret_2, ret_3, ret_4), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), ret_1, ret_2, ret_3, ret_4));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, R1, R2, R3, R4, R5> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, Callback.C19<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13), S.array(ret_1, ret_2, ret_3, ret_4, ret_5), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, R1, R2, R3, R4, R5, R6> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, Callback.C20<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, R1, R2, R3, R4, R5, R6, R7> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, Callback.C21<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, R1, R2, R3, R4, R5, R6, R7, R8> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, ResultDef<R8> ret_8, Callback.C22<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>, ResultDef<R8>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ParamDef<P14> def_14, Callback.C15<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13, def_14), S.array(), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), def_14.get(ctx)));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, R1> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ParamDef<P14> def_14, ResultDef<R1> ret_1, Callback.C16<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, ResultDef<R1>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13, def_14), S.array(ret_1), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), def_14.get(ctx), ret_1));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, R1, R2> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ParamDef<P14> def_14, ResultDef<R1> ret_1, ResultDef<R2> ret_2, Callback.C17<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, ResultDef<R1>, ResultDef<R2>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13, def_14), S.array(ret_1, ret_2), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), def_14.get(ctx), ret_1, ret_2));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, R1, R2, R3> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ParamDef<P14> def_14, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, Callback.C18<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13, def_14), S.array(ret_1, ret_2, ret_3), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), def_14.get(ctx), ret_1, ret_2, ret_3));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, R1, R2, R3, R4> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ParamDef<P14> def_14, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, Callback.C19<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13, def_14), S.array(ret_1, ret_2, ret_3, ret_4), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), def_14.get(ctx), ret_1, ret_2, ret_3, ret_4));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, R1, R2, R3, R4, R5> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ParamDef<P14> def_14, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, Callback.C20<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13, def_14), S.array(ret_1, ret_2, ret_3, ret_4, ret_5), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), def_14.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, R1, R2, R3, R4, R5, R6> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ParamDef<P14> def_14, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, Callback.C21<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13, def_14), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), def_14.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, R1, R2, R3, R4, R5, R6, R7> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ParamDef<P14> def_14, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, Callback.C22<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13, def_14), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), def_14.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, R1, R2, R3, R4, R5, R6, R7, R8> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ParamDef<P14> def_14, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, ResultDef<R8> ret_8, Callback.C23<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>, ResultDef<R8>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13, def_14), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), def_14.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ParamDef<P14> def_14, ParamDef<P15> def_15, Callback.C16<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13, def_14, def_15), S.array(), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), def_14.get(ctx), def_15.get(ctx)));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, R1> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ParamDef<P14> def_14, ParamDef<P15> def_15, ResultDef<R1> ret_1, Callback.C17<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, ResultDef<R1>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13, def_14, def_15), S.array(ret_1), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), def_14.get(ctx), def_15.get(ctx), ret_1));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, R1, R2> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ParamDef<P14> def_14, ParamDef<P15> def_15, ResultDef<R1> ret_1, ResultDef<R2> ret_2, Callback.C18<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, ResultDef<R1>, ResultDef<R2>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13, def_14, def_15), S.array(ret_1, ret_2), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), def_14.get(ctx), def_15.get(ctx), ret_1, ret_2));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, R1, R2, R3> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ParamDef<P14> def_14, ParamDef<P15> def_15, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, Callback.C19<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13, def_14, def_15), S.array(ret_1, ret_2, ret_3), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), def_14.get(ctx), def_15.get(ctx), ret_1, ret_2, ret_3));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, R1, R2, R3, R4> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ParamDef<P14> def_14, ParamDef<P15> def_15, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, Callback.C20<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13, def_14, def_15), S.array(ret_1, ret_2, ret_3, ret_4), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), def_14.get(ctx), def_15.get(ctx), ret_1, ret_2, ret_3, ret_4));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, R1, R2, R3, R4, R5> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ParamDef<P14> def_14, ParamDef<P15> def_15, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, Callback.C21<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13, def_14, def_15), S.array(ret_1, ret_2, ret_3, ret_4, ret_5), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), def_14.get(ctx), def_15.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, R1, R2, R3, R4, R5, R6> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ParamDef<P14> def_14, ParamDef<P15> def_15, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, Callback.C22<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13, def_14, def_15), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), def_14.get(ctx), def_15.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, R1, R2, R3, R4, R5, R6, R7> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ParamDef<P14> def_14, ParamDef<P15> def_15, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, Callback.C23<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13, def_14, def_15), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), def_14.get(ctx), def_15.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, R1, R2, R3, R4, R5, R6, R7, R8> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ParamDef<P14> def_14, ParamDef<P15> def_15, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, ResultDef<R8> ret_8, Callback.C24<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>, ResultDef<R8>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13, def_14, def_15), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), def_14.get(ctx), def_15.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ParamDef<P14> def_14, ParamDef<P15> def_15, ParamDef<P16> def_16, Callback.C17<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13, def_14, def_15, def_16), S.array(), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), def_14.get(ctx), def_15.get(ctx), def_16.get(ctx)));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, R1> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ParamDef<P14> def_14, ParamDef<P15> def_15, ParamDef<P16> def_16, ResultDef<R1> ret_1, Callback.C18<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, ResultDef<R1>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13, def_14, def_15, def_16), S.array(ret_1), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), def_14.get(ctx), def_15.get(ctx), def_16.get(ctx), ret_1));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, R1, R2> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ParamDef<P14> def_14, ParamDef<P15> def_15, ParamDef<P16> def_16, ResultDef<R1> ret_1, ResultDef<R2> ret_2, Callback.C19<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, ResultDef<R1>, ResultDef<R2>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13, def_14, def_15, def_16), S.array(ret_1, ret_2), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), def_14.get(ctx), def_15.get(ctx), def_16.get(ctx), ret_1, ret_2));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, R1, R2, R3> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ParamDef<P14> def_14, ParamDef<P15> def_15, ParamDef<P16> def_16, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, Callback.C20<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13, def_14, def_15, def_16), S.array(ret_1, ret_2, ret_3), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), def_14.get(ctx), def_15.get(ctx), def_16.get(ctx), ret_1, ret_2, ret_3));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, R1, R2, R3, R4> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ParamDef<P14> def_14, ParamDef<P15> def_15, ParamDef<P16> def_16, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, Callback.C21<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13, def_14, def_15, def_16), S.array(ret_1, ret_2, ret_3, ret_4), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), def_14.get(ctx), def_15.get(ctx), def_16.get(ctx), ret_1, ret_2, ret_3, ret_4));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, R1, R2, R3, R4, R5> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ParamDef<P14> def_14, ParamDef<P15> def_15, ParamDef<P16> def_16, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, Callback.C22<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13, def_14, def_15, def_16), S.array(ret_1, ret_2, ret_3, ret_4, ret_5), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), def_14.get(ctx), def_15.get(ctx), def_16.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, R1, R2, R3, R4, R5, R6> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ParamDef<P14> def_14, ParamDef<P15> def_15, ParamDef<P16> def_16, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, Callback.C23<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13, def_14, def_15, def_16), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), def_14.get(ctx), def_15.get(ctx), def_16.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, R1, R2, R3, R4, R5, R6, R7> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ParamDef<P14> def_14, ParamDef<P15> def_15, ParamDef<P16> def_16, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, Callback.C24<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13, def_14, def_15, def_16), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), def_14.get(ctx), def_15.get(ctx), def_16.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7));
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, R1, R2, R3, R4, R5, R6, R7, R8> APIHandler def(ParamDef<P1> def_1, ParamDef<P2> def_2, ParamDef<P3> def_3, ParamDef<P4> def_4, ParamDef<P5> def_5, ParamDef<P6> def_6, ParamDef<P7> def_7, ParamDef<P8> def_8, ParamDef<P9> def_9, ParamDef<P10> def_10, ParamDef<P11> def_11, ParamDef<P12> def_12, ParamDef<P13> def_13, ParamDef<P14> def_14, ParamDef<P15> def_15, ParamDef<P16> def_16, ResultDef<R1> ret_1, ResultDef<R2> ret_2, ResultDef<R3> ret_3, ResultDef<R4> ret_4, ResultDef<R5> ret_5, ResultDef<R6> ret_6, ResultDef<R7> ret_7, ResultDef<R8> ret_8, Callback.C25<HttpCtx, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, ResultDef<R1>, ResultDef<R2>, ResultDef<R3>, ResultDef<R4>, ResultDef<R5>, ResultDef<R6>, ResultDef<R7>, ResultDef<R8>> handler) {
        return new APIHandler(S.array(def_1, def_2, def_3, def_4, def_5, def_6, def_7, def_8, def_9, def_10, def_11, def_12, def_13, def_14, def_15, def_16), S.array(ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8), ctx -> handler.apply(ctx, def_1.get(ctx), def_2.get(ctx), def_3.get(ctx), def_4.get(ctx), def_5.get(ctx), def_6.get(ctx), def_7.get(ctx), def_8.get(ctx), def_9.get(ctx), def_10.get(ctx), def_11.get(ctx), def_12.get(ctx), def_13.get(ctx), def_14.get(ctx), def_15.get(ctx), def_16.get(ctx), ret_1, ret_2, ret_3, ret_4, ret_5, ret_6, ret_7, ret_8));
    }

}