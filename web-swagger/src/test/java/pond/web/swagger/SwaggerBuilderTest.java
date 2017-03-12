package pond.web.swagger;

import pond.web.Pond;
import pond.web.restful.API;
import pond.web.restful.ParamDef;
import pond.web.restful.ResultDef;


/**
 * Created by edwin on 3/10/2017.
 */
public class SwaggerBuilderTest {

    public static class MyAPI extends API {
        {
            get("/:id", API.def(
                    ParamDef.path("id"),
                    ResultDef.text("id"),
                    (ctx, id, send) -> {
                        ctx.result(send, id);
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