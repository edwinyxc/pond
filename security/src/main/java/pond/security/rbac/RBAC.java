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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class RBAC {

  final DB db;

  String lb_user_id = "id";
  String lb_user_name = "username";

  String lb_role_id = "id";
  String lb_role_name= "username";
//  final Function.F0<String> _get_user;

  public RBAC(String policy_name) {
    this.db = new DB(ConnectionPool.c3p0(ConnectionPool.local(policy_name)));
    db.post(this::_init);
  }

  public RBAC(DB db){
    this.db = db;
    db.post(this::_init);
  }

  public RBAC label_role_id(String lb_id) {
    this.lb_role_id = lb_id;
    return this;
  }

  public RBAC label_rolename(String lb_name) {
    this.lb_role_name= lb_name;
    return this;
  }

  public RBAC label_user_id(String lb_id) {
    this.lb_user_id = lb_id;
    return this;
  }

  public RBAC label_username(String lb_name) {
    this.lb_user_name= lb_name;
    return this;
  }

  public RBAC sync(List<Record> users, List<Record> roles){
    return syncUsers(users).syncRoles(roles);
  }

  private RBAC syncRoles(List<Record> roles) {

    List<Record> current_all = role_all();
    List<String> current_all_ids = S._for(current_all).map(u -> (String) u.get(lb_role_id)).toList();
    List<String> roles_ids = S._for(roles).map(u -> (String) u.get(lb_role_id)).toList();

    db.post(t -> {
      //cut the outer side
      S._for(current_all_ids).each(cid -> {
        if (!roles_ids.contains(cid)) {
          role_del(cid, t);
        }
      });

      //set brand new
      S._for(roles).each(role -> {
        if (!current_all_ids.contains((String) role.get(lb_role_id))) {
          role_add(role.get(lb_role_id), S.avoidNull(role.get(lb_role_name), ""), t);
        }
      });

    });

    return this;
  }
  private RBAC syncUsers(List<Record> users) {

    List<Record> current_all = user_all();
    List<String> current_all_ids = S._for(current_all).map(u -> (String) u.get(lb_user_id)).toList();
    List<String> users_ids = S._for(users).map(u -> (String) u.get(lb_user_id)).toList();

    db.post(t -> {
      //cut the outer side
      S._for(current_all_ids).each(cid -> {
        if (!users_ids.contains(cid)) {
          user_del(cid, t);
        }
      });

      //set brand new
      S._for(users).each(user -> {
        if (!current_all_ids.contains((String) user.get(lb_user_id))) {
          user_add(user.get(lb_user_id), S.avoidNull(user.get(lb_user_name),""), t);
        }
      });

    });

    return this;
  }

  private void _init(JDBCTmpl t) {
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
    });
    return this;
  }

  public DB db(){
    return db;
  }

  //user

  public List<Record> user_all() {
    return db.get(t -> t.query("SELECT id FROM rbac_user"));
  }

  public List<Record> role_all() {
    return db.get(t -> t.query("SELECT id FROM rbac_role"));
  }

  public void user_add(String uid, String name, JDBCTmpl t) {

    if (t.count("SELECT COUNT(*) FROM rbac_user WHERE id = ?", uid) > 0)
      throw new RuntimeException("User@" + uid + " already exists");
    else
      t.exec("INSERT INTO rbac_user VALUES(?,?)", uid, name);
  }

  public void user_del(String uid, JDBCTmpl t) {

    t.exec("DELETE FROM rbac_user_has_role WHERE user_id = ?", uid);
    t.exec("DELETE FROM rbac_user WHERE id = ?", uid);
  }

  public void user_add_role(String uid, String rid, JDBCTmpl t) {
    int count = t.count("SELECT COUNT(*) FROM rbac_user_has_role WHERE user_id = ? and role_id = ?", uid, rid);
    if (count > 0) return;
    t.exec("INSERT INTO rbac_user_has_role VALUES(?,?)", uid, rid);
  }

  public void user_del_role(String uid, String rid, JDBCTmpl t) {
    t.exec("DELETE FROM rbac_user_has_role WHERE user_id = ? and role_id = ?", uid, rid);
  }

  public boolean user_exists(String uid) {
    return db.get(t -> t.count("SELECT COUNT(*) FROM rbac_user WHERE id = ?", uid)) > 0;
  }

  public void user_upd_name(String uid, String name, JDBCTmpl t) {
    if (user_exists(uid))
      t.exec("UPDATE rbac_user SET username = ? WHERE id = ?", name, uid);
    else throw new RuntimeException("User@" + uid + " not found");
  }

  public void role_add(String rid, String rolename, JDBCTmpl t) {
    if (t.count("SELECT COUNT(*) FROM rbac_role WHERE id = ?", rid) > 0)
      throw new RuntimeException("Role@" + rid + " already exists");
    else
      t.exec("INSERT INTO rbac_role VALUES(?,?)", rid, rolename);
  }

  public void role_del(String rid, JDBCTmpl t) {
    t.exec("DELETE FROM rbac_role_has_service WHERE role_id = ?", rid);
    t.exec("DELETE FROM rbac_role WHERE id = ?", rid);
  }

  public void role_upd(String rid, String rolename, JDBCTmpl t) {
    if (t.count("SELECT COUNT(*) FROM rbac_role WHERE id = ?", rid) > 0)
      t.exec("UPDATE rbac_user SET rolename = ? WHERE id = ?", rolename, rid);
    else throw new RuntimeException("Role@" + rid + " not found");
  }


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

      db.post(t -> RBAC.this.role_add(rid, rolename, t));
      resp.send(200, rid);
    }

    @Mapping(value = "/roles/:rid", methods = HttpMethod.DELETE)
    public void role_del(Request req, Response resp) {
      String rid = req.param("rid");
      db.post(t -> RBAC.this.role_del(rid, t));
      resp.send(200, rid);
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
      db.post(t -> RBAC.this.user_add(uid, name, t));
      resp.send(200);
    }

    @Mapping(value = "/users/:uid", methods = HttpMethod.DELETE)
    public void user_del(Request req, Response resp) {
      String uid = req.param("uid");
      db.post(t -> RBAC.this.user_del(uid, t));
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

      String sql = "SELECT r.id, rolename " +
          "FROM rbac_role r LEFT JOIN rbac_user_has_role ur ON r.id = ur.role_id " +
          "WHERE ur.user_id = ? ";

      List<Record> roles = RBAC.this.db.get(sql, uid);

//      user.set("roles", roles);

      resp.render(Render.json(roles));
    }

    @Mapping(value = "/users/:uid/roles", methods = HttpMethod.POST)
    public void user_roles_add(Request req, Response resp) {
      String uid = req.param("uid");

      if (STRING.isBlank(uid)) {
        resp.send(400, "uid null");
        return;
      }

      List<Record> l = RBAC.this.db.get("SELECT * FROM rbac_user WHERE id = ?", uid);

      if (l.size() < 1) {
        resp.send(404, "user not found");
        return;
      }

      String rid = req.param("rid");

      if (STRING.isBlank(rid)) {
        resp.send(400, "rid null");
        return;
      }

      List<Record> lr = RBAC.this.db.get("SELECT * FROM rbac_role WHERE id = ?", rid);
      if (lr.size() < 1) {
        resp.send(404, "role not found");
        return;
      }

      db.post(t -> user_add_role(uid, rid, t));

      resp.send(200);
    }


  };

  public Roles forUser(String user) {
    int user_count = db.get(t -> t.count("SELECT COUNT(*) FROM rbac_user_has_role WHERE user_id = ?", user));
    if (user_count < 1) {
      return new Roles(Collections.emptyList());
    }

    return new Roles(S._for(db.get("SELECT role_id FROM rbac_user_has_role WHERE user_id = ?", user))
                         .map(record -> (String) record.get("role_id")).toList());
  }

  public class Roles {

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
