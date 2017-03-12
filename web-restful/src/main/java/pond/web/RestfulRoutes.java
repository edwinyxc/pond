package pond.web;

import pond.common.JSON;
import pond.common.S;
import pond.common.f.Tuple;
import pond.db.DB;
import pond.db.Model;
import pond.db.Prototype;
import pond.db.Record;
import pond.db.sql.Sql;
import pond.db.sql.SqlSelect;
import pond.web.restful.API;
import pond.web.restful.ParamDef;
import pond.web.restful.ResultDef;
import pond.web.restful.APIHandler;

import java.util.List;

import static pond.db.sql.Criterion.*;

/**
 * Created by ed on 6/9/16.
 */
public class RestfulRoutes<E extends Model> {
    Router router;
    DB db;
    E proto;

    public final APIHandler index;
    public final APIHandler getById;
    public final APIHandler update;
    public final APIHandler create;
    public final APIHandler delete;

    public RestfulRoutes(Router router, DB db, E proto) {
        this.router = router;
        this.db = db;
        this.proto = proto;

        index = API.def(
                ParamDef.composeAs("proto", proto.toMap()).to(req -> Sql.selectFromRequest(req, proto)),
                ResultDef.page("paged rows"),
                (ctx, sql, ret) -> {
                    S.echo("debug sql", sql);
                    db.post(t -> ctx.result(ret, Tuple.pair(t.query(sql), t.count(sql.count()))));
                });

        getById = API.def(
                ParamDef.path("id").required("require non-null on field: id"),
                ResultDef.json("Found Item"),
                (ctx, id, ret) -> {
                    SqlSelect sql = Sql.select("*").from(proto.table()).where(proto.idName(), EQ, id);
                    ctx.result(ret, db.get(t -> t.query(sql)));
                });

        update = API.def(
                ParamDef.path("id").required("require non-null on field: id"),
                ParamDef.composeAs("proto", proto.toMap()),

                ResultDef.json("Updated Item"),
                ResultDef.error(404, "Item not found"),
                ResultDef.error(500, "Item not found"),

                (ctx, id, requestMap, ret_updated, not_found, internal) -> {
                    SqlSelect sqlSelect = Sql.select("*").from(proto.table()).where(proto.idName(), EQ, id);
                    List<E> l = db.get(t -> t.query((Class<E>) proto.getClass(), sqlSelect));
                    if (l.size() < 1) {
                        ctx.result(not_found, "id:" + id + " item not found");
                        return;
                    }
                    E e = (E) l.get(0);
                    e.merge(requestMap);
                    db.post(t -> t.recordUpdate(e));
                    ctx.result(ret_updated, e);
                });

        create = API.def(
                ParamDef.composeAs("proto", proto.toMap()),
                ResultDef.json(201, "Created Item"),
                (ctx, reqMap, ret_created) -> {
                    E e = (E) Record.newEntity(proto.getClass());
                    db.post(t -> t.recordInsert(e.merge(reqMap)));
                    ctx.result(ret_created, e);
                }
        );

        delete = API.def(
                ParamDef.path("id").required("require non-null on field: id"),

                ResultDef.json(204, "Deleted"),
                ResultDef.errorf(404, "Not Found Item %s"),

                (ctx, id, deleted, not_found) -> {
                    SqlSelect sqlSelect = Sql.select("*").from(proto.table()).where(proto.idName(), EQ, id);
                    List<E> l = db.get(t -> t.query((Class<E>) proto.getClass(), sqlSelect));
                    if (l.size() < 1)
                        ctx.result(not_found, new String[]{id});
                    E e = (E) l.get(0);
                    db.post(t -> t.recordDelete(e));
                    ctx.result(deleted, "");
                }
        );
    }

    public RestfulRoutes(Router router, DB db, Class<E> protoClass) {
        this(router, db, Prototype.proto(protoClass));
    }

    public RestfulRoutes<E> index(Mid cb) {
        router.get("/", cb);
        return this;
    }

    public RestfulRoutes<E> index(CtxHandler cb) {
        router.get("/", cb);
        return this;
    }

    public RestfulRoutes<E> id(Mid cb) {
        router.get("/:id", cb);
        return this;
    }

    public RestfulRoutes<E> id(CtxHandler cb) {
        router.get("/:id", cb);
        return this;
    }

    public RestfulRoutes<E> postRoot(Mid cb) {
        router.post("/", cb);
        return this;
    }

    public RestfulRoutes<E> postRoot(CtxHandler cb) {
        router.post("/", cb);
        return this;
    }

    public RestfulRoutes<E> putRoot(Mid cb) {
        router.put("/", cb);
        return this;
    }

    public RestfulRoutes<E> putRoot(CtxHandler cb) {
        router.put("/", cb);
        return this;
    }

    public RestfulRoutes<E> delRoot(Mid cb) {
        router.del("/:id", cb);
        return this;
    }

    public RestfulRoutes<E> delRoot(CtxHandler cb) {
        router.del("/:id", cb);
        return this;
    }

    public RestfulRoutes<E> id() {
        id(getById);
        return this;
    }

    public RestfulRoutes<E> index() {
        index(index);
        return this;
    }

    public RestfulRoutes<E> postRoot() {
        postRoot(create);
        return this;
    }

    public RestfulRoutes<E> putRoot() {
        putRoot(update);
        return this;
    }

    public RestfulRoutes<E> delRoot() {
        delRoot(delete);
        return this;
    }

    public RestfulRoutes<E> all() {
        id(getById);
        index(index);
        postRoot(create);
        putRoot(update);
        delRoot(delete);
        return this;
    }
}
