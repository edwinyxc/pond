//package pond.web.swagger;
//
//
//import pond.web.Router;
//import pond.web.restful.API;
//import pond.web.restful.APIHandler;
//import pond.web.restful.Path;
//import pond.web.restful.ResultDef;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static pond.common.f.Tuple.pair;
//import static pond.web.restful.ParamDef.*;
//import static pond.web.restful.ResultDef.*;
//
///**
// * Swagger Json spec v2
// */
//public class Swagger extends HashMap<String, Object> {
//    {
//        this.put("swagger", "2.0");
//    }
//
//
//    public Swagger info(Info info) {
//        this.put("info", info);
//        return this;
//    }
//
//    public Swagger host(String host) {
//        this.put("host", host);
//        return this;
//    }
//
//    public Swagger basePath(String basePath) {
//        this.put("basePath", basePath);
//        return this;
//    }
//
//    public Swagger paths(Map<String, Path> paths) {
//        this.put("paths", paths);
//        return this;
//    }
//
//
//    public Swagger tags(List<Tag> tags) {
//        this.put("tags", tags);
//        return this;
//    }
//
//    public Swagger schemes(List<String> schemes) {
//        this.put("schemes", schemes);
//        return this;
//    }
//
//    public static Swagger buildAPI(Router api) {
//
//        Swagger swagger = new Swagger();
//        if(api instanceof API){
//            API _api = (API) api;
//            swagger.basePath(_api.basePath());
//            swagger.info(new Info().title(_api.title)
//                    .version(_api.version)
//                    .description(_api.desc));
//            swagger.paths(_api.getAllPathsRecursively());
//            return swagger;
//        }
//        else {
//            throw new IllegalArgumentException("not a valid API");
//        }
//    }
//
//    public static APIHandler swaggerJSON(Router api) {
//
//        return API.def(
//                ResultDef.json(200, "Swagger JSON file v2.0.0"),
//                (ctx, render) -> {
//                    ctx.result(render, buildAPI(api));
//                }
//        );
//    }
//
//    public static CtxHandler server() {
//        return new API() {
//            final Class swaggerClass = Swagger.class;
//            final String swaggerPrefix = "swagger-ui/";
//
//            {
//                getEntry("/", API.def(
//                        resourceAsFile("index.html"),
//                        (ctx, render) ->
//                                ctx.result(render, pair(swaggerClass, swaggerPrefix + "index.html"))
//                ));
//                getEntry("/:file", API.def(
//                        path("file"),
//                        resourceAsFile("file"),
//                        (ctx, file, render) ->
//                                ctx.result(render, pair(swaggerClass, swaggerPrefix + file))
//                ));
//                getEntry("/css/:file", API.def(
//                        path("file"),
//                        resourceAsFile("css files"),
//                        (ctx, file, render) ->
//                                ctx.result(render, pair(swaggerClass, swaggerPrefix + "css/" + file))
//                ));
//                getEntry("/fonts/:file", API.def(
//                        path("file"),
//                        resourceAsFile("fonts"),
//                        (ctx, file, render) ->
//                                ctx.result(render, pair(swaggerClass, swaggerPrefix + "fonts/" + file))
//                ));
//                getEntry("/images/:file", API.def(
//                        path("file"),
//                        resourceAsFile("images"),
//                        (ctx, file, render) ->
//                                ctx.result(render, pair(swaggerClass, swaggerPrefix + "images/" + file))
//                ));
//                getEntry("/lang/:file", API.def(
//                        path("file"),
//                        resourceAsFile("lang"),
//                        (ctx, file, render) ->
//                                ctx.result(render, pair(swaggerClass, swaggerPrefix + "lang/" + file))
//                ));
//                getEntry("/lib/:file", API.def(
//                        path("file"),
//                        resourceAsFile("lib"),
//                        (ctx, file, render) ->
//                                ctx.result(render, pair(swaggerClass, swaggerPrefix + "lib/" + file))
//                ));
//
//                otherwise(API.def(
//                        error(404, "404"),
//                        (ctx, e404) -> ctx.result(e404, "not found")
//                ));
//            }
//        };
//    }
//
//
//}
//
//class Info extends HashMap<String, Object> {
//    public Info description(String description) {
//        this.put("description", description);
//        return this;
//    }
//
//    public Info version(String version) {
//        this.put("version", version);
//        return this;
//    }
//
//    public Info title(String title) {
//        this.put("title", title);
//        return this;
//    }
//}
//
//class Tag extends HashMap<String, Object> {
//    public Tag name(String name) {
//        this.put("name", name);
//        return this;
//    }
//
//    public Tag description(String description) {
//        this.put("description", description);
//        return this;
//    }
//}
//
