package pond.db.connpool;

import pond.common.S;

public class MysqlSimplePoolBuilder {

  String mysqlDriver = "com.mysql.jdbc.Driver";
  int capacity = 10;
  String host;
  String database;
  String query;
  String username;
  String password;

  public MysqlSimplePoolBuilder() {
  }

  public MysqlSimplePoolBuilder host(String host) {
    this.host = host;
    return this;
  }

  public MysqlSimplePoolBuilder database(String database) {
    this.database = database;
    return this;
  }

  public MysqlSimplePoolBuilder query(String query) {
    this.query = query;
    return this;
  }

  public MysqlSimplePoolBuilder username(String username) {
    this.username = username;
    return this;
  }

  public MysqlSimplePoolBuilder password(String password) {
    this.password = password;
    return this;
  }

  public MysqlSimplePoolBuilder capacity(int i) {
    this.capacity = i;
    return this;
  }

  public SimplePool build() {
    S._assert(username, "please input username");
    S._assert(password, "please input password");
    S._assert(host, "please input host");
    S._assert(database, "please input database");
    return S._tap(new SimplePool().config(mysqlDriver,
            String.format("jdbc:mysql://%s/%s?%s", host, database, S.avoidNull(query, "")),
            username, password),
        newpool -> newpool.setMaxSize(capacity));
  }

}
