package pond.web.api.contract;

import pond.common.S;
import pond.core.Ctx;
import pond.web.Request;
import pond.web.Response;
import pond.web.api.contract.Contract.*;
import pond.web.api.contract.Contract.RouteConfig.Methods.GET;
import pond.web.api.contract.Contract.RouteConfig.Route;
import pond.web.api.contract.Contract.RouteConfig.RoutePrefix;
import pond.web.http.HttpCtx;

import java.util.Map;

@RoutePrefix("/api")
@Summary("hello there")
@Description("I hate reflection")
public class MyDummy {

    private int count = 0;

    public String ok(Request req, Response resp) {
        return "OK";
    }

    @GET
    public Integer read(){
        return count;
    }

    @GET
    @Route("add/:add")
    public Integer add_and_get(Integer add){
        return count += add;
    }

    @GET
    @Produces("application/json")
    public Object basic_route(String name, String value){
        return new Object(){
            public String _name = name;
            public String _value = value;
        };
    }

    @GET
    @Route("_debug")
    public String debug(HttpCtx ctx){
        return S.dump(ctx.request().headers());
    }


    @Ignore
    public String basic_route2(Request req, Response resp){
        return "OK";
    }
}
