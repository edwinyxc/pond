package pond.db;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pond.common.S;
import pond.common.f.Holder;
import pond.db.connpool.SimplePool;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by ed on 15-6-30.
 */
public class ConcurrentTest {

    DB db;
    DataSource dataSource;

    @Before
    public void init() throws PropertyVetoException, ClassNotFoundException {

//        S._debug_on(DB.class);

//        Class.forName("org.h2.Driver");
//        JdbcDataSource ds = new JdbcDataSource();
//        ds.setURL("jdbc:h2:~/test");
//        ds.setUser("sa");
//        ds.setPassword("sa");


        this.dataSource = new SimplePool() {
            {
                this.setMaxSize(20);
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
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        List<CompletableFuture> futures = new ArrayList<>();

        long s = S.now();
        S._repeat(() -> {
            futures.add(CompletableFuture.runAsync(
                    () -> {
                        db.post(tmpl -> {
                            tmpl.exec("USE POND_DB_TEST;");
                            for (int i = 0; i < 400; i++)
                                tmpl.exec("INSERT INTO test values(?,?)",
                                        String.valueOf(Math.random()), String.valueOf(val.accum()));
                        });

                    }, executorService));
        }, 1000);

        try {
            CompletableFuture.allOf(S._for(futures).join()).thenRun(() -> {
                S._for((List<Record>) db.get(t -> {
                    t.exec("USE POND_DB_TEST;");
                    return t.query("SELECT count(*) FROM test");
                })).each(S::echo);
                S.echo("ALL FINISHED : time usage " + (S.now() - s));
            }) .get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


    }

    @After
    public void after() {
        db.post(tmpl -> tmpl.exec(
                "DROP DATABASE IF EXISTS POND_DB_TEST;"
        ));
    }
}
