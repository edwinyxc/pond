package pond.web;

import pond.common.f.Holder;
import pond.db.DB;
import pond.db.Prototype;
import pond.db.Record;
import pond.db.sql.Criterion;
import pond.db.sql.Sql;
import pond.db.sql.SqlSelect;

import java.util.List;

/**
 * Created by ed on 3/17/16.
 * Restful Router for pond
 * default get, put, post, delete
 */
public class RestfulRouter<E extends Record> extends Router {

  final E proto;

  final DB db;

  private Mid queryById;
  private Mid queryAll;
  private Mid create;

  private Mid update;
  private Mid delete;

  @SuppressWarnings("unchecked")
  public RestfulRouter(Class<E> cls, DB db) {
    this.db = db;
    this.proto = Prototype.proto(cls);

    queryById = (req, resp) -> {
      String id = req.paramNonBlank("id", "require non-null on field: id");
      SqlSelect sql = Sql.select("*").from(proto.table()).where(proto.idName(), Criterion.EQ, id);
      resp.render(Render.json(db.get(t -> t.query(sql))));
    };

    queryAll = (req, resp) -> {
      SqlSelect sql = Sql.selectFromRequest(req.toMap(), proto);
      Holder<Integer> count = new Holder<>();
      List ret = db.get(t -> {
        List r = t.query(sql);
        count.val(t.count(sql.count()));
        return r;
      });
      resp.render(Render.page(ret, count.val()));
    };

    create = (req, resp) -> {
      E e = (E) Record.newEntity(proto.getClass());
      db.post(t -> t.recordInsert(e.merge(req.toMap())));
      resp.render(Render.json(e));
    };

    update = (req, resp) -> {

      String id = req.paramNonBlank("id", "require non-null on field: id");
      SqlSelect sqlSelect = Sql.select("*").from(proto.table()).where(proto.idName(), Criterion.EQ, id);
      List<Record> l = db.get(t -> t.query(sqlSelect));
      if (l.size() < 1)
        throw new EndToEndException(404, "id:" + id + " item not found");
      E e = (E) l.get(0);
      e.merge(req.toMap());
      db.post(t -> t.recordUpdate(e));
      resp.send(200);
    };

    delete =  (req, resp) -> {
      String id = req.paramNonBlank("id", "require non-null on field: id");
      SqlSelect sqlSelect = Sql.select("*").from(proto.table()).where(proto.idName(), Criterion.EQ, id);
      List<Record> l = db.get(t -> t.query(sqlSelect));
      if (l.size() < 1)
        throw new EndToEndException(404, "id:" + id + " item not found");
      E e = (E) l.get(0);
      e.merge(req.toMap());
      db.post(t -> t.recordDelete(e));
      resp.send(201);
    };

    get("/:id", queryById);

    get("/", queryAll);

    post("/", create);

    put("/:id",update);

    del("/:id",delete);
  }

  public void setQueryById(Mid queryById) {
    this.queryById = queryById;
  }

  public void setQueryAll(Mid queryAll) {
    this.queryAll = queryAll;
  }

  public void setCreate(Mid create) {
    this.create = create;
  }

  public void setUpdate(Mid update) {
    this.update = update;
  }

  public void setDelete(Mid delete) {
    this.delete = delete;
  }
}
