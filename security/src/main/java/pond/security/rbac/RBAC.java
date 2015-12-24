package pond.security.rbac;

import pond.common.S;
import pond.common.STRING;
import pond.db.DB;
import pond.db.JDBCTmpl;
import pond.db.Record;
import pond.db.connpool.ConnectionPool;
import pond.web.Controller;
import pond.web.Render;
import pond.web.Request;
import pond.web.Response;
import pond.web.http.HttpMethod;

import java.util.*;

public class RBAC {

  final String name;
  final ConnectionPool cp;
  final DB db;
//  final Function.F0<String> _get_user;

  public RBAC(String policy_name) {
    this.name = policy_name;
    this.cp = ConnectionPool.c3p0(ConnectionPool.local(policy_name));
    this.db = new DB(cp);
//    this._get_user = get_user;
    db.post(this::init);
  }

  void init(JDBCTmpl t) {
    //rbac_user_has_role
    t.exec("CREATE TABLE IF NOT EXISTS rbac_user(id varchar(64) primary key, username varchar(255))");
    t.exec("CREATE TABLE IF NOT EXISTS rbac_role(id varchar(64) primary key, rolename varchar(255))");
    t.exec("CREATE TABLE IF NOT EXISTS rbac_user_has_role(user_id varchar(64), role_id varchar(64), PRIMARY KEY(user_id, role_id))");

  }

  public RBAC reset() {
    db.post(t -> {
      t.exec("DROP TABLE IF EXISTS rbac_user_has_role");
      t.exec("DROP TABLE IF EXISTS rbac_user");
      t.exec("DROP TABLE IF EXISTS rbac_role");
      init(t);
    });
    return this;
  }

  //user
  public void user_add(String uid, String name) {

    db.post(t -> {
      if (t.count("SELECT COUNT(*) FROM rbac_user WHERE id = ?", uid) > 0)
        throw new RuntimeException("User@" + uid + " already exists");
      else
        t.exec("INSERT INTO rbac_user VALUES(?,?)", uid, name);
    });
  }

  public void user_del(String uid) {

    db.post(t -> {
      t.exec("DELETE FROM rbac_user_has_role WHERE user_id = ?", uid);
      t.exec("DELETE FROM rbac_user WHERE id = ?", uid);
    });
  }

  public void user_add_role(String uid, String rid) {
    db.post(t -> {
      int count = t.count("SELECT COUNT(*) FROM rbac_user_has_role WHERE user_id = ? and role_id = ?", uid, rid);
      if (count > 0) return;
      t.exec("INSERT INTO rbac_user_has_role VALUES(?,?)", uid, rid);
    });
  }

  public void user_del_role(String uid, String rid) {
    db.post(t -> {
      t.exec("DELETE FROM rbac_user_has_role WHERE user_id = ? and role_id = ?", uid, rid);
    });
  }

  public void user_upd_name(String uid, String name) {
    db.post(t -> {
      if (t.count("SELECT COUNT(*) FROM rbac_user WHERE id = ?", uid) > 0)
        t.exec("UPDATE rbac_user SET username = ? WHERE id = ?", name, uid);
      else throw new RuntimeException("User@" + uid + " not found");
    });
  }

  public void role_add(String rid, String description) {
    db.post(t -> {
      if (t.count("SELECT COUNT(*) FROM rbac_role WHERE id = ?", rid) > 0)
        throw new RuntimeException("Role@" + rid + " already exists");
      else
        t.exec("INSERT INTO rbac_role VALUES(?,?)", rid, description);
    });
  }

  public void role_del(String rid) {
    db.post(t -> {
      t.exec("DELETE FROM rbac_role_has_service WHERE role_id = ?", rid);
      t.exec("DELETE FROM rbac_role WHERE id = ?", rid);
    });
  }

  public void role_upd(String rid, String description) {
    db.post(t -> {
      if (t.count("SELECT COUNT(*) FROM rbac_role WHERE id = ?", rid) > 0)
        t.exec("UPDATE rbac_user SET username = ? WHERE id = ?", description, rid);
      else throw new RuntimeException("Role@" + rid + " not found");
    });
  }

//  public void role_add_serv(String rid, String serv) {
//
//    db.post(t -> {
//      int count = t.count("SELECT COUNT(*) FROM rbac_role_has_service WHERE role_id = ? and service = ?", rid, serv);
//      if (count > 0) return;
//      t.exec("INSERT INTO rbac_role_has_service VALUES(?,?)", rid, serv);
//    });
//  }
//
//  public void role_del_serv(String rid, String serv) {
//
//    db.post(t -> {
//      t.exec("DELETE FROM rbac_role_has_service WHERE role_id = ? and service = ?", rid, serv);
//    });
//  }

//  public List<String> distinct_services_on_user(String uid) {
//    return S._for(db.get("SELECT DISTINCT service FROM rbac_user_has_service WHERE id = ?", uid))
//        .map(r -> (String) r.get("service")).toList();
//  }

  //web-controllers
  public final Controller controller = new Controller() {

    @Mapping(value = "/roles", methods = HttpMethod.GET)
    public void roles_get(Request req, Response resp) {
      resp.render(Render.json(RBAC.this.db.get("SELECT * FROM rbac_role")));
    }

    @Mapping(value = "/roles", methods = HttpMethod.POST)
    public void roles_add(Request req, Response resp) {
      String rid = req.param("id");
      String rolename = req.param("rolename");

      RBAC.this.role_add(rid, rolename);
      resp.send(200);
    }

    @Mapping(value = "/roles/:rid", methods = HttpMethod.DELETE)
    public void role_del(Request req, Response resp) {
      String rid = req.param("rid");
      RBAC.this.role_del(rid);
      resp.send(200);
    }

    @Mapping(value = "/roles/:rid", methods = HttpMethod.GET)
    public void role_get(Request req, Response resp) {
      String rid = req.param("rid");
      if (STRING.isBlank(rid)) {
        resp.send(404, "not found");
        return;
      }

      List<Record> l = RBAC.this.db.get("SELECT * FROM rbac_role WHERE id = ?", rid);

      if (l.size() < 1) {
        resp.send(404, "not found");
        return;
      }
      resp.render(Render.json(l.get(0)));
    }


    @Mapping(value = "/users", methods = HttpMethod.GET)
    public void users(Request req, Response resp) {
      resp.render(Render.json(RBAC.this.db.get("SELECT * FROM rbac_user")));
    }

    @Mapping(value = "/users", methods = HttpMethod.POST)
    public void user_add(Request req, Response resp) {
      String uid = req.param("uid");
      String name = req.param("username");
      RBAC.this.user_add(uid, name);
      resp.send(200);
    }

    @Mapping(value = "/users/:uid", methods = HttpMethod.DELETE)
    public void user_del(Request req, Response resp) {
      String uid = req.param("uid");
      RBAC.this.user_del(uid);
      resp.send(200);
    }

    @Mapping(value = "/users/:uid", methods = HttpMethod.GET)
    public void user_get(Request req, Response resp) {
      String uid = req.param("uid");
      if (STRING.isBlank(uid)) {
        resp.send(404, "not found");
        return;
      }
      List<Record> l = RBAC.this.db.get("SELECT * FROM rbac_user WHERE id = ?", uid);
//      String sql = "SELECT u.id,username,ur.role_id,description " +
//          "FROM rbac_user u LEFT JOIN rbac_user_has_role ur ON u.id = r.user_id " +
//          "LEFT JOIN rbac_role r ON ur.role_id = r.id WHERE u.id = ?";
      if (l.size() < 1) {
        resp.send(404, "not found");
        return;
      }
      resp.render(Render.json(l.get(0)));
    }

    @Mapping(value = "/users/:uid/roles", methods = HttpMethod.GET)
    public void user_roles(Request req, Response resp) {
      String uid = req.param("uid");
      if (STRING.isBlank(uid)) {
        resp.send(404, "not found");
        return;
      }

      List<Record> l = RBAC.this.db.get("SELECT * FROM rbac_user WHERE id = ?", uid);

      if (l.size() < 1) {
        resp.send(404, "not found");
        return;
      }

//      Record user = l.get(0);

      String sql = "SELECT r.id,description " +
          "FROM rbac_role r LEFT JOIN rbac_user_has_role ur ON r.id = r.role_id " +
          "WHERE ur.user_id = ? ";

      List<Record> roles = RBAC.this.db.get(sql, uid);

//      user.set("roles", roles);

      resp.render(Render.json(roles));
    }

    @Mapping(value = "/users/:uid/roles", methods = HttpMethod.POST)
    public void user_roles_add(Request req, Response resp) {
      String uid = req.param("uid");
      if (STRING.isBlank(uid)) {
        resp.send(404, "not found");
        return;
      }

      List<Record> l = RBAC.this.db.get("SELECT * FROM rbac_user WHERE id = ?", uid);

      if (l.size() < 1) {
        resp.send(404, "not found");
        return;
      }

//      //role [id, ]
//      S._for(req.params("roles"))
//          .map(JSON::parse)
//          .map(m -> S._for(RBAC.this.db.post("INSERT INTO rbac_user_has_role SET  id = ?", (String) m.get("id"))).first())
//          .compact()
//          .each(r -> RBAC.this.user_add_role(uid, r.get("id")));

      resp.send(200);
    }

    @Mapping(value = "/users/:uid/roles/:rid", methods = HttpMethod.GET)
    public void user_roles_del(Request req, Response resp) {
      String uid = req.param("uid");
      String rid = req.param("rid");

      if (STRING.isBlank(uid)) {
        resp.send(404, "not found");
        return;
      }

      List<Record> l = RBAC.this.db.get("SELECT * FROM rbac_user WHERE id = ?", uid);

      if (l.size() < 1) {
        resp.send(404, "not found");
        return;
      }

      RBAC.this.user_del_role(uid, rid);

      resp.send(200);
    }

  };

   public Roles forUser(String user) {
    int user_count = db.get(t -> t.count("SELECT COUNT(*) FROM rbac_user_has_role WHERE user_id = ?", user));
    if( user_count < 1){
      return new Roles(Collections.emptyList());
    }

    return new Roles(S._for(db.get("SELECT role_id FROM rbac_user_has_role WHERE user_id = ?", user))
        .map(record -> (String) record.get("role_id")).toList());
  }

  public class Roles{

    List<String> _roles = new LinkedList<>();

    Roles(Collection<String> roles) {
      this._roles.addAll(roles);
    }

    public boolean hasEvery(String... roles) {

      return S._for(roles).every(_roles::contains);
    }

    public boolean hasAny(String... roles) {
      return S._for(roles).some(_roles::contains);
    }

    public boolean hasNone(String... roles) {
      return S._for(roles).every(r -> (!_roles.contains(r)));
    }

    @Override
    public String toString() {
      return "Roles{" +
          "_roles=" + _roles +
          '}';
    }
  }






//  //interceptor
//  public Interceptor interceptor() {
//    return (ctx, serv) -> {
//      String user = how_to_get_user.apply(ctx);
//
//      if (db.get(t -> t.count(
//          "SELECT COUNT(*) FROM rbac_user_has_service WHERE id = ? and service = ?",
//          user, serv.name())) < 1) {
//        ctx.stop();
//        ctx.err("Access Denied@" + serv.name());
//      }
//
//    };
//  }


}
