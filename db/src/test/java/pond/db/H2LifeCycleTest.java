package pond.db;

import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.Server;
import org.junit.Test;
import pond.common.S;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by ed on 15-6-30.
 */
public class H2LifeCycleTest {
    @Test
    public void start() throws SQLException, ClassNotFoundException {
        Class.forName("org.h2.Driver");
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:~/test");
        ds.setUser("sa");
        ds.setPassword("sa");
        DB db = new DB(ds);
        db.post(t -> t.exec("drop table t_test if exists"));
        db.post(t -> t.exec("create table t_test (text varchar(30))"));
        S._times(() -> db.post(t -> t.exec("insert into t_test values('?')", String.valueOf(Math.random()))), 10);
        //S._times(() -> db.post(t -> t.execRaw("insert into t_test values('"+Math.random()+"')")), 10);
        List<Record> result = db.get(t -> t.query("select * from t_test"));
        List<Record> result1 = db.get(t -> t.query("select * from t_test"));
        List<Record> result2 = db.get(t -> t.query("select * from t_test"));
        S.echo(S.dump(result));
        S.echo(S.dump(result1));
        S.echo(S.dump(result2
        ));
    }
}
