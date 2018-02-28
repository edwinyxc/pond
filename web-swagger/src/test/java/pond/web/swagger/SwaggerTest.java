package pond.web.swagger;

import org.junit.Ignore;
import pond.common.S;
import pond.web.Pond;
import pond.web.restful.API;
import pond.web.restful.APIHandler;
import pond.web.restful.ParamDef;
import pond.web.restful.ResultDef;

import java.util.*;

import static org.junit.Assert.*;

@Ignore
public class SwaggerTest {

    public static void swagger_integration() {
        APIHandler auth = API.def(
                        ParamDef.header("Authorization"),
                        ResultDef.error(403, "Forbidden"),
                        (ctx, header, forbidden) -> {
                            if("Ohh".equals(header)){
                                S.echo("Passed Ohh");
                                //ok
                            }else {
                                ctx.result(forbidden, "");
                            }
                        }
                );

        Pond.init(p -> {
            p.get("/", API.def(
                    ParamDef.header("X-header"),
                    ResultDef.text("X-header echo"),
                    (ctx, xheader, echo) -> {
                        ctx.result(echo, "!!!" + xheader);
                    }

            ));
            p.use("/xb/*", auth, new API(){{

                get("/", API.def(
                        ParamDef.header("X-header"),
                        ResultDef.text("X-header echo"),
                        (ctx, xheader, echo) -> {
                            ctx.result(echo, "!!!" + xheader);
                        }
                ));

                post("/", API.def(
                        ParamDef.header("X-header"),
                        ResultDef.text("X-header echo"),
                        (ctx, xheader, echo) -> {
                            ctx.result(echo, "!!!" + xheader);
                        }
                ));

                use("/inner/*", new API(){{
                    get("/x", API.def(
                            ParamDef.header("X-header"),
                            ResultDef.text("X-header echo"),
                            (ctx, xheader, echo) -> {
                                ctx.result(echo, "!!!" + xheader);
                            }
                    ));
                }});

            }});
            p.get("/x", auth, API.def(
                    ResultDef.text("Normal output of some texts"),
                    (ctx, txt) -> {
                        ctx.result(txt, "Now, you're safe x!!");
                    }
            ));
            p.get("/b", auth, API.def(
                    ResultDef.text("Normal output of some texts"),
                    (ctx, txt) -> {
                        ctx.result(txt, "Now, you're safe b!!");
                    }
            ));
            //test
            p.get("/swagger/api/p", Swagger.swaggerJSON(p));
            p.get("/swagger/*", Swagger.server());

        }).listen();
    }

    public static void main(String[] args) {
        swagger_integration();
    }

}