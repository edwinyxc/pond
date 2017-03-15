package pond.web.swagger;

import pond.web.Pond;
import pond.web.restful.API;
import pond.web.restful.ParamDef;
import pond.web.restful.ResultDef;
import pond.web.restful.Schema;

import java.util.HashMap;


/**
 * Created by edwin on 3/10/2017.
 */
public class SwaggerBuilderTest {

    public static class MyAPI extends API {
        {
            use("/inner/*",
                    new API() {{
                        post("/la", API.def(
                                ParamDef.param("name"),
                                ParamDef.param("password"),
                                ResultDef.error(403, "forbidden"),
                                (ctx, name, password, forbidden) -> {
                                    if (name.equals("1") && password.equals("1")) {
                                        ctx.set("name", name);
                                    } else ctx.result(forbidden, "");
                                }
                        ), API.def(
                                ParamDef.form("email"),
                                ParamDef.query("to").required(""),

                                ResultDef.error(403, "custom 403"),

                                ResultDef.json("normal out").schema(Schema.OBJECT(
                                        new HashMap<String, Object>() {{
                                            put("name", "");
                                            put("email", "");
                                            put("to", "");
                                        }}
                                )),
                                (ctx, email, to, forbidden, json) -> {
                                    String name = (String) ctx.get("name");
                                    ctx.result(json, new HashMap<String, Object>() {{
                                        put("name", name);
                                        put("email", email);
                                        put("to", to);
                                    }});
                                }
                        ));

                        get("/inner2/*", new API() {{
                            get("/", API.def(
                                    ParamDef.param("id"),
                                    (ctx, id) -> {
                                    }
                            ));

                            get("/complex", API.def(

                                    ParamDef.param("name"),
                                    ParamDef.param("password"),
                                    ResultDef.error(403, "forbidden"),
                                    (ctx, name, password, forbidden) -> {
                                        if (name.equals("1") && password.equals("1")) {
                                            ctx.set("name", name);
                                        } else ctx.result(forbidden, "");
                                    }
                            ), API.def(
                                    ParamDef.form("email"),
                                    ParamDef.query("to").required(""),

                                    ResultDef.error(403, "custom 403"),

                                    ResultDef.json("normal out").schema(Schema.OBJECT(
                                            new HashMap<String, Object>() {{
                                                put("name", "");
                                                put("email", "");
                                                put("to", "");
                                            }}
                                    )),
                                    (ctx, email, to, forbidden, json) -> {
                                        String name = (String) ctx.get("name");
                                        ctx.result(json, new HashMap<String, Object>() {{
                                            put("name", name);
                                            put("email", email);
                                            put("to", to);
                                        }});
                                    }


                            ));
                        }});
                    }});


            get("/:id", API.def(
                    ParamDef.path("id"),

                    ResultDef.text("id"),
                    (ctx, id, send) -> {
                        ctx.result(send, id);
                    }
            ));

            post("/test", API.def(
                    ParamDef.param("name"),
                    ParamDef.param("password"),
                    ResultDef.error(403, "forbidden"),
                    (ctx, name, password, forbidden) -> {
                        if (name.equals("1") && password.equals("1")) {
                            ctx.set("name", name);
                        } else ctx.result(forbidden, "");
                    }
            ), API.def(
                    ParamDef.form("email"),
                    ParamDef.query("to").required(""),

                    ResultDef.error(403, "custom 403"),

                    ResultDef.json("normal out").schema(Schema.OBJECT(
                            new HashMap<String, Object>() {{
                                put("name", "");
                                put("email", "");
                                put("to", "");
                            }}
                    )),
                    (ctx, email, to, forbidden, json) -> {
                        String name = (String) ctx.get("name");
                        ctx.result(json, new HashMap<String, Object>() {{
                            put("name", name);
                            put("email", email);
                            put("to", to);
                        }});
                    }
            ));
        }
    }

    //    @Test
    public void buildForRoute() throws Exception {

//        System.out.println(JSON.stringify(SwaggerBuilder.buildAPI(new MyAPI())));
    }

    public static void main(String[] args) {

//        URL uri = Swagger.class.getClassLoader().getResource("swagger-ui");
//        S.echo(uri);

        Pond.init(p -> {
            p.get("/swagger/api/my", Swagger.swaggerJSON(p));
            p.get("/swagger/*", Swagger.server());
            p.use("/my/*", new MyAPI());
            p.use("/*", new MyAPI());
        }).listen(9090);
    }

}