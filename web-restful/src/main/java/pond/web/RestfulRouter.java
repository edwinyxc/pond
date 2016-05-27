package pond.web;

import pond.common.JSON;
import pond.db.DB;
import pond.db.Prototype;
import pond.db.Record;
import pond.db.sql.Criterion;
import pond.db.sql.Sql;
import pond.db.sql.SqlSelect;
import pond.web.http.MimeTypes;

import java.util.List;

/**
 * Created by ed on 3/17/16.
 * Restful Router for pond
 * default get, put, post, delete
 */
public class RestfulRouter<E extends Record> extends Router {

  final E proto;

  final DB db;

  final REST REST = new REST();

  @SuppressWarnings("unchecked")
  public RestfulRouter(Class<E> cls, DB db) {
    this.db = db;
    this.proto = Prototype.proto(cls);
  }

  protected class REST {
    final Mid getAll = (req, resp) -> {
      SqlSelect sql = Sql.selectFromRequest(req.toMap(), proto);
      db.post(t -> resp.render(Render.page(t.query(sql), t.count(sql.count()))));
    };

    final Mid getById = (req, resp) -> {
      String id = req.paramNonBlank("id", "require non-null on field: id");
      SqlSelect sql = Sql.select("*").from(proto.table()).where(proto.idName(), Criterion.EQ, id);
      resp.render(Render.json(db.get(t -> t.query(sql))));
    };

    final Mid update = (req, resp) -> {
      String id = req.paramNonBlank("id", "require non-null on field: id");
      SqlSelect sqlSelect = Sql.select("*").from(proto.table()).where(proto.idName(), Criterion.EQ, id);
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


    final Mid create = (req, resp) -> {
      @SuppressWarnings("unchecked")
      E e = (E) Record.newEntity(proto.getClass());
      db.post(t -> t.recordInsert(e.merge(req.toMap())));
      resp.contentType(MimeTypes.MIME_APPLICATION_JSON);
      resp.send(201, JSON.stringify(e));
    };


    final Mid delete = (req, resp) -> {
      String id = req.paramNonBlank("id", "require non-null on field: id");
      SqlSelect sqlSelect = Sql.select("*").from(proto.table()).where(proto.idName(), Criterion.EQ, id);
      List<Record> l = db.get(t -> t.query(sqlSelect));
      if (l.size() < 1)
        throw new EndToEndException(404, "id:" + id + " item not found");
      @SuppressWarnings("unchecked")
      E e = (E) l.get(0);
      e.merge(req.toMap());
      db.post(t -> t.recordDelete(e));
      resp.send(204);
    };

    public RestfulRouter<E> getAll(Mid cb) {
      get("/", cb);
      return RestfulRouter.this;
    }

    public RestfulRouter<E> getById(Mid cb) {
      get("/:id", cb);
      return RestfulRouter.this;
    }

    public RestfulRouter<E> create(Mid cb) {
      post("/", cb);
      return RestfulRouter.this;
    }

    public RestfulRouter<E> update(Mid cb) {
      put("/", cb);
      return RestfulRouter.this;
    }

    public RestfulRouter<E> delete(Mid cb) {
      del("/:id", cb);
      return RestfulRouter.this;
    }

    public RestfulRouter<E> all(){
      getById(getById);
      getAll(getAll);
      create(create);
      update(update);
      delete(delete);
      return RestfulRouter.this;
    }

  }

}
