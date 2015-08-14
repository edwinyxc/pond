package pond.db.connpool;

import pond.common.S;

public class H2SimplePoolBuilder {

  String driver = "org.h2.Driver";
  int capacity = 10;
  String host;
  String database;
  String query;
  String username;
  String password;

  public H2SimplePoolBuilder() {
  }

  public H2SimplePoolBuilder host(String host) {
    this.host = host;
    return this;
  }

  public H2SimplePoolBuilder database(String database) {
    this.database = database;
    return this;
  }

  public H2SimplePoolBuilder query(String query) {
    this.query = query;
    return this;
  }

  public H2SimplePoolBuilder username(String username) {
    this.username = username;
    return this;
  }

  public H2SimplePoolBuilder password(String password) {
    this.password = password;
    return this;
  }

  public H2SimplePoolBuilder capacity(int i) {
    this.capacity = i;
    return this;
  }

  public SimplePool build() {
    S._assert(username, "please input username");
    S._assert(password, "please input password");
    S._assert(host, "please input host");
    S._assert(database, "please input database");
    return S._tap(new SimplePool().config(driver,
                                          String.format("jdbc:h2://%s/%s?%s", host, database, S.avoidNull(query, "")),
                                          username, password),
                  newpool -> newpool.setMaxSize(capacity));
  }

}
