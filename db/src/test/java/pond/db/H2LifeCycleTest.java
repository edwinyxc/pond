package pond.db;

import org.h2.jdbcx.JdbcDataSource;
import pond.common.S;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by ed on 15-6-30.
 */
public class H2LifeCycleTest {
    public void start() throws SQLException, ClassNotFoundException {
        Class.forName("org.h2.Driver");
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:~/test");
        ds.setUser("sa");
        ds.setPassword("sa");
        DB db = new DB(ds);
        db.post("drop table t_test if exists");
        db.post("create table t_test (text varchar(30))");
        S._repeat(() -> db.post(t -> t.exec("insert into t_test values(?)", String.valueOf(Math.random()))), 10);
        //S._repeat(() -> db.post(t -> t.execRaw("insert into t_test values('"+Math.random()+"')")), 10);
        List<Record> result = db.get("select * from t_test");
        long cost = S.time(() ->
            S._repeat(() -> db.get("select  * from t_test"), 100)
        );
        S.echo(cost);
        S.echo(S.dump(result));
    }
}
