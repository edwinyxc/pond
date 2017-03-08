package pond.web;

import pond.common.JSON;
import pond.common.f.Tuple;
import pond.db.DB;
import pond.db.Model;
import pond.db.Prototype;
import pond.db.Record;
import pond.db.sql.Sql;
import pond.db.sql.SqlSelect;
import pond.web.http.MimeTypes;

import java.util.List;

import static pond.db.sql.Criterion.*;

/**
 * Created by ed on 6/9/16.
 */
public class RestfulRoutes<E extends Model> {
    Router router;
    DB db;
    E proto;


    public final WellDefinedHandler index = CtxHandler.def(
            ParamDef.requestToMap(),
            ResultDef.page("paged rows"),
            (ctx, map, ret) -> {
                SqlSelect sql = Sql.selectFromRequest(map, proto);
                db.post(t -> ctx.result(ret, Tuple.pair(t.query(sql), t.count(sql.count()))));
            });

    public final WellDefinedHandler getById = CtxHandler.def(
            ParamDef.str("id").required("require non-null on field: id"),
            ResultDef.json("Found Item"),
            (ctx, id, ret) -> {
                SqlSelect sql = Sql.select("*").from(proto.table()).where(proto.idName(), EQ, id);
                ctx.result(ret, db.get(t -> t.query(sql)));
            });

    @SuppressWarnings("unchecked")
    public final WellDefinedHandler update = CtxHandler.def(
            ParamDef.str("id").required("require non-null on field: id"),
            ParamDef.requestToMap(),

            ResultDef.json("Updated Item"),
            ResultDef.error(404, "Item not found"),
            ResultDef.error(500, "Item not found"),

            (ctx, id, search, ret_updated, not_found, internal) -> {
                SqlSelect sqlSelect = Sql.select("*").from(proto.table()).where(proto.idName(), EQ, id);
                List<E> l = db.get(t -> t.query((Class<E>) proto.getClass(), sqlSelect));
                if (l.size() < 1) {
                    ctx.result(not_found, "id:" + id + " item not found");
                    return;
                }
                E e = (E) l.get(0);
                e.merge(search);
                db.post(t -> t.recordUpdate(e));
                ctx.result(ret_updated, e);
            });


    @SuppressWarnings("unchecked")
    public final WellDefinedHandler create = CtxHandler.def(
            ParamDef.requestToMap(),
            ResultDef.json(201, "Created Item"),
            (ctx, search, ret_created) -> {
                E e = (E) Record.newEntity(proto.getClass());
                db.post(t -> t.recordInsert(e.merge(search)));
                ctx.result(ret_created, JSON.stringify(e));
            }
    );

    @SuppressWarnings("unchecked")
    public final WellDefinedHandler delete = CtxHandler.def(
            ParamDef.str("id").required("require non-null on field: id"),
            ParamDef.requestToMap(),
            ResultDef.json(204, "Deleted"),
            ResultDef.errorf(404, "Not Found Item %s"),
            (ctx, id, map, ret, not_found) -> {
                SqlSelect sqlSelect = Sql.select("*").from(proto.table()).where(proto.idName(), EQ, id);
                List<E> l = db.get(t -> t.query((Class<E>) proto.getClass(), sqlSelect));
                if (l.size() < 1)
                    ctx.result(not_found, new String[]{id});
                E e = (E) l.get(0);
                e.merge(map);
                db.post(t -> t.recordDelete(e));
                ctx.result(ret, "");
            }
    );

    public RestfulRoutes(Router router, DB db, E proto) {
        this.router = router;
        this.db = db;
        this.proto = proto;
    }

    public RestfulRoutes(Router router, DB db, Class<E> protoClass) {
        this.router = router;
        this.db = db;
        this.proto = Prototype.proto(protoClass);
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
