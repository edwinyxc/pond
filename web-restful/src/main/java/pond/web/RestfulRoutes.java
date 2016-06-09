package pond.web;

import pond.common.JSON;
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


  public final Mid index = (req, resp) -> {
    SqlSelect sql = Sql.selectFromRequest(req.toMap(), proto);
    db.post(t -> resp.render(Render.page(t.query(sql), t.count(sql.count()))));
  };

  public final Mid getById = (req, resp) -> {
    String id = req.paramNonBlank("id", "require non-null on field: id");
    SqlSelect sql = Sql.select("*").from(proto.table()).where(proto.idName(), EQ, id);
    resp.render(Render.json(db.get(t -> t.query(sql))));
  };

  public final Mid update = (req, resp) -> {
    String id = req.paramNonBlank("id", "require non-null on field: id");
    SqlSelect sqlSelect = Sql.select("*").from(proto.table()).where(proto.idName(), EQ, id);
    List<Record> l = db.get(t -> t.query(sqlSelect));
    if (l.size() < 1)
      throw new EndToEndException(404, "id:" + id + " item not found");
    @SuppressWarnings("unchecked")
    E e = (E) l.get(0);
    e.merge(req.toMap());
    db.post(t -> t.recordUpdate(e));
    resp.contentType(MimeTypes.MIME_APPLICATION_JSON);
    resp.send(200, JSON.stringify(e));
  };

  public final Mid create = (req, resp) -> {
    @SuppressWarnings("unchecked")
    E e = (E) Record.newEntity(proto.getClass());
    db.post(t -> t.recordInsert(e.merge(req.toMap())));
    resp.contentType(MimeTypes.MIME_APPLICATION_JSON);
    resp.send(201, JSON.stringify(e));
  };

  public final Mid delete = (req, resp) -> {
    String id = req.paramNonBlank("id", "require non-null on field: id");
    SqlSelect sqlSelect = Sql.select("*").from(proto.table()).where(proto.idName(), EQ, id);
    List<Record> l = db.get(t -> t.query(sqlSelect));
    if (l.size() < 1)
      throw new EndToEndException(404, "id:" + id + " item not found");
    @SuppressWarnings("unchecked")
    E e = (E) l.get(0);
    e.merge(req.toMap());
    db.post(t -> t.recordDelete(e));
    resp.send(204);
  };

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

  public RestfulRoutes<E> id(Mid cb) {
    router.get("/:id", cb);
    return this;
  }

  public RestfulRoutes<E> postRoot(Mid cb) {
    router.post("/", cb);
    return this;
  }

  public RestfulRoutes<E> putRoot(Mid cb) {
    router.put("/", cb);
    return this;
  }

  public RestfulRoutes<E> delRoot(Mid cb) {
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

  public RestfulRoutes<E> putRoot(){
    putRoot(update);
    return this;
  }

  public RestfulRoutes<E> delRoot() {
    delRoot(delete);
    return this;
  }

  public RestfulRoutes<E> all(){
    id(getById);
    index(index);
    postRoot(create);
    putRoot(update);
    delRoot(delete);
    return this;
  }
}
