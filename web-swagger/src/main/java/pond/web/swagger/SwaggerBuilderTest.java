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
       //            get("/:id", API.def(
//                    ParamDef.path("id"),
//                    ResultDef.text("id"),
//                    (ctx, id, send) -> {
//                        ctx.result(send, id);
//                    }
//            ));

            post("/test", API.def(
                    ParamDef.param("name").toInteger(),
                    ParamDef.form("email"),
                    ParamDef.query("to").required(""),

                    ResultDef.json("name&email&to").schema(Schema.OBJECT(
                            new HashMap<String, Object>(){{
                            put("name", "");
                            put("email", "");
                            put("to", "");
                        }}
                    )),

                    (ctx, name, email, to, json) ->{

                        ctx.result(json, new HashMap<String, Object>(){{
                            put("name", name);
                            put("email",email);
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
        API MY = new MyAPI();
        Pond.init(p -> {
            p.get("/swagger/api/my", Swagger.swaggerJSON(MY));
            p.get("/swagger/*", Swagger.server());
            p.use("/*", MY);
        }).listen(9090);
    }

}