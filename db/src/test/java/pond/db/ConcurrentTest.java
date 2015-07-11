package pond.db;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pond.common.S;
import pond.common.f.Holder;
import pond.db.connpool.SimplePool;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by ed on 15-6-30.
 */
public class ConcurrentTest {

    DB db;
    DataSource dataSource;

    @Before
    public void init() throws PropertyVetoException, ClassNotFoundException {

//        S._debug_on(DB.class);

        //h2
//        Class.forName("org.h2.Driver");
//        JdbcDataSource ds = new JdbcDataSource();
//        ds.setURL("jdbc:h2:~/test");
//        ds.setUser("sa");
//        ds.setPassword("sa");

//        this.dataSource = new SimplePool() {
//            {
//                this.setMaxSize(20);
//                this.config(
//                        "org.h2.Driver",
//                        "jdbc:h2:~/test",
//                        "sa",
//                        "sa");
//            }
//        };

        this.dataSource = new SimplePool() {
            {
                this.setMaxSize(100);
                this.config(
                        "com.mysql.jdbc.Driver",
                        "jdbc:mysql://127.0.0.1:3306/",
                        "root",
                        "root");
            }
        };

        this.db = new DB(dataSource);

        db.batch(
                "DROP DATABASE IF EXISTS POND_DB_TEST;",
                "CREATE DATABASE POND_DB_TEST;",
                "USE POND_DB_TEST;",
                "CREATE TABLE test ( id varchar(64) primary key, `value` varchar(2000) );",
                "INSERT INTO test values('2333','233333');",
                "INSERT INTO test values('2334','2333334');"
        );

    }


    @Test
    public void testExecTx() {
        Holder.AccumulatorInt val = new Holder.AccumulatorInt(0);
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        long s = S.now();
        S._times(() -> {
            try {
                Future o = executorService.submit(() -> {
                    db.post(tmpl -> {
                        tmpl.exec("USE POND_DB_TEST;");
                        tmpl.exec("INSERT INTO test values(?,?)", String.valueOf(Math.random()), String.valueOf(val.accum()));
                        tmpl.exec("INSERT INTO test values(?,?)", String.valueOf(Math.random()), String.valueOf(val.accum()));
                        tmpl.exec("INSERT INTO test values(?,?)", String.valueOf(Math.random()), String.valueOf(val.accum()));
                        tmpl.exec("INSERT INTO test values(?,?)", String.valueOf(Math.random()), String.valueOf(val.accum()));
                    });
                    return db.get(t -> {
                        t.exec("USE POND_DB_TEST;");
                        return t.query("SELECT * FROM test");
                    });
                });
                S.echo(o.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }, 100);

        S.echo("time usage " + (S.now() - s));
    }

    @After
    public void after() {
        db.post(tmpl -> tmpl.exec(
                "DROP DATABASE IF EXISTS POND_DB_TEST;"
        ));
    }
}
