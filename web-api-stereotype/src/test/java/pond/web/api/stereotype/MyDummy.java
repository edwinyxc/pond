package pond.web.api.stereotype;

import pond.common.S;
import pond.web.Request;
import pond.web.Response;
import pond.web.api.stereotype.Contract.*;
import pond.web.api.stereotype.Contract.RouteConfig.Methods.GET;
import pond.web.api.stereotype.Contract.RouteConfig.Route;
import pond.web.api.stereotype.Contract.RouteConfig.RoutePrefix;
import pond.web.http.HttpCtx;
import pond.web.router.RouterCtx;

@RoutePrefix("/api")
@Summary("hello there")
@Description("I hate reflection")
public class MyDummy {
    private int count = 0;

    public String ok(Request req, Response resp) {
        return "OK";
    }

    public String ok2(HttpCtx ctx){
        var rctx = (RouterCtx) ctx::bind;
        return "";
    }

    @GET
    @Route("rename")
    public Integer test_name_override(HttpCtx ctx, Request req, Response resp) {
        assert ctx != null && req != null && resp != null;
        return 0;
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
    public Object basic_route(String name, String value ){
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
