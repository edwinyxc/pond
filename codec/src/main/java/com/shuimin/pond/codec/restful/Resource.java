package com.shuimin.pond.codec.restful;

import com.shuimin.common.S;
import com.shuimin.common.f.Function;
import com.shuimin.common.f.Holder;
import com.shuimin.pond.core.db.Record;
import com.shuimin.pond.codec.mvc.Controller;
import com.shuimin.pond.core.Global;
import com.shuimin.pond.core.Middleware;
import com.shuimin.pond.core.Pond;
import com.shuimin.pond.core.Request;
import com.shuimin.pond.core.http.HttpMethod;
import com.shuimin.pond.core.mw.Action;

import java.io.File;
import java.util.Arrays;

import static com.shuimin.common.S._for;
import static com.shuimin.common.f.Tuple.T3;
import static com.shuimin.common.f.Tuple.t3;
import static com.shuimin.pond.core.Interrupt.render;
import static com.shuimin.pond.core.Pond.debug;
import static com.shuimin.pond.core.Renderable.view;
import static com.shuimin.pond.core.http.HttpMethod.mask;

/**
 * Created by ed on 14-5-20.
 */
public class Resource extends Controller {

    /**
     * GET ${bo}/?[query]   -- list as query text/html index.view || application/json
     */
    public static final Function<T3<Integer, String, Middleware>, Resource> INDEX =
            res -> t3(mask(HttpMethod.GET), "/", Action.simple((req, resp) ->
            {
                ResourceService service = res.service.val;
                String mime = getAcceptHeader(req).trim().toLowerCase();
                if (mime.startsWith("text/html"))
                    render(view(res.resourcePath("index.view"), service.query(req)));
                else
                    render(service.render(mime, service.query((req))));

            }));
    /**
     * POST ${bo}/?[params] -- create
     */
    public static final Function<T3<Integer, String, Middleware>, Resource> CREATE = res ->
            t3(mask(HttpMethod.POST), "/", Action.simple((req, resp) -> res.service.val.create(req)));
    /**
     * GET ${bo}/${id} -- get by id text/html detail.view || application/json
     */
    public final static Function<T3<Integer, String, Middleware>, Resource> GET = res ->
            t3(mask(HttpMethod.GET), "/${_id}"
                    , Action.simple((req, resp) ->
            {
                String id = req.param("_id");
                String mime = getAcceptHeader(req).trim().toLowerCase();
                if (mime.startsWith("text/html"))
                    render(view(res.resourcePath("detail.view"), res.service.val.get(id)));
                else
                    render(res.service.val.render(mime, res.service.val.get(id)));
            }));
    /**
     * DELETE ${bo}/${id} --delete
     */
    public final static Function<T3<Integer, String, Middleware>, Resource> DELETE = res ->
            t3(mask(HttpMethod.DELETE), "/${_id}",
                    Action.simple((req, resp) -> {
                        String id = req.param("_id");
                        res.service.val.delete(id);
                    })
            );
    /**
     * PUT ${bo}/${id} --update
     */
    public final static Function<T3<Integer, String, Middleware>, Resource> UPDATE = res ->
            t3(mask(HttpMethod.PUT), "/${_id}",
                    Action.simple((req, resp) -> {
                        ResourceService service = res.service.val;
                        String id = req.param("_id");
                        service.update(id, req);
                    })
            );
    /**
     * GET ${bo}/new -- new view ONLY ACCEPT text/html new.view
     */
    public final Function<T3<Integer, String, Middleware>, Resource> VIEW_NEW = res ->
            t3(mask(HttpMethod.GET), "/new",
                    Action.fly(() -> render(view(resourcePath("new.view"))))
            );
    /**
     * GET ${bo}/${id}/edit -- edit view ACCEPT text/html edit.view
     */
    public final Function<T3<Integer, String, Middleware>, Resource> VIEW_EDIT = res ->
            t3(mask(HttpMethod.GET), "/${_id}/edit",
                    Action.simple((req, resp) -> {
                        String id = req.param("_id");
                        Object o = res.service.val.get(id);
                        render(view(resourcePath("edit.view"), o));
                    })
            );
    protected final Holder<ResourceService> service = new Holder<>();

    private Resource(ResourceService service) {
        this.service.val = service;
    }

    static String getAcceptHeader(Request req) {
        String accept = _for(req.header("Accept")).first();
        debug("Accept:" + accept);
        return S.str.notBlank(accept) ? accept : "text/html";
    }

    public static Resource build(Record proto,
                                 Function<T3<Integer, String, Middleware>, Resource>... actions) {
        Resource ret = new Resource(new ResourceService() {
            @Override
            Record prototype() {
                return proto;
            }
        });
        if (actions == null || actions.length == 0)
            return ret.initByDefault();
        else ret.actions.addAll(Arrays.asList(_for(actions).map(a -> a.apply(ret)).join()));
        return ret;
    }

    public static Resource build(Function.F0<Record> func,
                                 Function<T3<Integer, String, Middleware>, Resource>... actions) {
        return build(func.apply(),actions);
    }

    public ResourceService service() {
        return service.val;
    }

    String templatePath() {
        return Pond.attribute(Global.TEMPLATE_PATH);
    }

    private Resource initByDefault() {

        this.actions.add(UPDATE.apply(this));
        this.actions.add(CREATE.apply(this));
        this.actions.add(VIEW_NEW.apply(this));
        this.actions.add(VIEW_EDIT.apply(this));
        this.actions.add(DELETE.apply(this));
        this.actions.add(GET.apply(this));
        this.actions.add(INDEX.apply(this));

        return this;
    }

    String resourcePath(String name) {
        return File.separator + nameSupplier.apply(this) + File.separator + name;
    }

}
