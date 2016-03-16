package pond.db;


import org.junit.Before;
import org.junit.Test;
import pond.common.S;
import pond.db.connpool.ConnectionPool;
import pond.db.sql.Sql;
import pond.db.sql.SqlSelect;

import java.util.HashMap;
import java.util.Map;

public class PaginationTest {

  DB db;

  @Before
  public void init() {
    S._debug_on(DB.class, JDBCTmpl.class);

    this.db = new DB(ConnectionPool.c3p0(ConnectionPool.local("test")));

    db.batch(
        "DROP TABLE IF EXISTS test_pagination",
        "CREATE TABLE test_pagination ( " +
            " name varchar(2000), " + //string eq lk
            " birthday bigint(11), " + // between
            " type varchar(30), " + //in
            " )",
        "INSERT INTO test_pagination values('yxc',123456,'admin')",
        "INSERT INTO test_pagination values('yxc',123456,'admin')",
        "INSERT INTO test_pagination values('yxc',123456,'admin')",
        "INSERT INTO test_pagination values('yxc',123456,'admin')",
        "INSERT INTO test_pagination values('yxc',123456,'admin')",
        "INSERT INTO test_pagination values('yxc',123456,'admin')",
        "INSERT INTO test_pagination values('yxc',123456,'admin')",
        "INSERT INTO test_pagination values('yxc',123456,'admin')",
        "INSERT INTO test_pagination values('yxc',123456,'admin')",
        "INSERT INTO test_pagination values('yxc',123456,'admin')",
        "INSERT INTO test_pagination values('yxc',123456,'admin')",

        "INSERT INTO test_pagination values('sky',123457,'debugger')",
        "INSERT INTO test_pagination values('sky',123457,'debugger')",
        "INSERT INTO test_pagination values('sky',123457,'debugger')",
        "INSERT INTO test_pagination values('sky',123457,'debugger')",
        "INSERT INTO test_pagination values('sky',123457,'debugger')",
        "INSERT INTO test_pagination values('sky',123457,'debugger')",
        "INSERT INTO test_pagination values('sky',123457,'debugger')",
        "INSERT INTO test_pagination values('sky',123457,'debugger')",
        "INSERT INTO test_pagination values('sky',123457,'debugger')",
        "INSERT INTO test_pagination values('sky',123457,'debugger')",
        "INSERT INTO test_pagination values('sky',123457,'debugger')",
        "INSERT INTO test_pagination values('skyyy',123457,'debugger')",
        "INSERT INTO test_pagination values('skyyy',123457,'debugger')",
        "INSERT INTO test_pagination values('skyyy',123457,'debugger')",
        "INSERT INTO test_pagination values('skyyy',123457,'debugger')",
        "INSERT INTO test_pagination values('skyyy',123457,'debugger')",
        "INSERT INTO test_pagination values('skyyy',123457,'debugger')",
        "INSERT INTO test_pagination values('skyyy',123457,'debugger')",
        "INSERT INTO test_pagination values('skyyy',123457,'debugger')",
        "INSERT INTO test_pagination values('skyyy',123457,'debugger')",
        "INSERT INTO test_pagination values('skyyy',123457,'debugger')",
        "INSERT INTO test_pagination values('skyyy',123457,'debugger')",
        "INSERT INTO test_pagination values('yxccc',123200,'super_admin')",
        "INSERT INTO test_pagination values('yxccc',123200,'super_admin')",
        "INSERT INTO test_pagination values('yxccc',123200,'super_admin')",
        "INSERT INTO test_pagination values('yxccc',123200,'super_admin')",
        "INSERT INTO test_pagination values('yxccc',123200,'super_admin')",
        "INSERT INTO test_pagination values('yxccc',123200,'super_admin')",
        "INSERT INTO test_pagination values('yxccc',123200,'super_admin')",
        "INSERT INTO test_pagination values('yxccc',123200,'super_admin')",
        "INSERT INTO test_pagination values('yxccc',123200,'super_admin')",
        "INSERT INTO test_pagination values('yxccc',123200,'super_admin')",
        "INSERT INTO test_pagination values('yxccc',123200,'super_admin')",
        "INSERT INTO test_pagination values('ed',222222,'foo')",
        "INSERT INTO test_pagination values('ed',222222,'foo')",
        "INSERT INTO test_pagination values('ed',222222,'foo')",
        "INSERT INTO test_pagination values('ed',222222,'foo')",
        "INSERT INTO test_pagination values('ed',222222,'foo')",
        "INSERT INTO test_pagination values('ed',222222,'foo')",
        "INSERT INTO test_pagination values('ed',222222,'foo')",
        "INSERT INTO test_pagination values('ed',222222,'foo')",
        "INSERT INTO test_pagination values('ed',222222,'foo')",
        "INSERT INTO test_pagination values('ed',222222,'foo')",
        "INSERT INTO test_pagination values('ed',222222,'foo')"
    );

  }

  public Map<String, Object> q1() {
    Map<String, Object> map = new HashMap<>();
    map.put("name", "yxccc");//name == yxccc
    return map;
  }

  public Map<String, Object> p1() {
    Map<String, Object> map = new HashMap<>();
    map.put("name", "yxccc");//name == yxccc
    map.put("page", "4");
    map.put("rows", "3");
    return map;
  }

  public Map<String, Object> q2() {

    Map<String, Object> map = new HashMap<>();
    map.put("name", "lk,sky");//name == yxccc
    map.put("birthday", "btwn,123201,123457");// between
    map.put("type", "in,foo,super_admin");// between
    return map;
  }

  Record proto = Prototype.proto(TestModel.class);

  public void testReqToQuery() throws Exception {
  }

  public void testReqToOrders() throws Exception {

  }

  @Test
  public void testSqlFromReq() throws Exception {

    SqlSelect select = Sql.select().from(TestModel.class);

    S.echo(Pagination.sqlFromReq(q1(), proto).apply(select));

    S.echo(Pagination.sqlFromReq(q2(), proto).apply(select));
  }

  @Test
  public void testQueryForPage() throws Exception {

    SqlSelect select = Sql.select().from(TestModel.class);

    Object result =
        db.get(t -> Pagination.queryForPage(p1(), select, proto).apply(t));

    S.echo("s1", result);

//    S.echo(Pagination.sqlFromReq(q2(), proto).apply(select));
  }


}