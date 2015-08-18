package pond.db;

import pond.db.connpool.SimplePool;

import javax.sql.DataSource;

/**
 * Created by ed on 6/24/15.
 */
public class TestTransaction {
  //tested in our studio server
  public static DataSource localDataSource = SimplePool.mysql()
      .host("localhost")
      .database("test")
      .username("root")
      .password("root").build();


  public static void main(String[] args) {
    DB db = new DB(localDataSource);
    db.post("update user_group set gid = '0' ", "update user_group ddd");
  }

}
