package pond.db;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pond.common.S;
import pond.db.connpool.ConnectionPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ed on 15-6-30.
 */
public class ConcurrentTest {

  DB db;
  ConnectionPool dataSource;

  @Before
  public void init()throws ClassNotFoundException {

    this.dataSource = ConnectionPool.c3p0(ConnectionPool.local("test"));

    this.db = new DB(dataSource);

    db.batch(
        "DROP TABLE IF EXISTS test",
        "CREATE TABLE test ( id varchar(64) primary key, `value` varchar(2000) );",
        "INSERT INTO test values('2333','233333');",
        "INSERT INTO test values('2334','2333334');"
    );
    S._debug_on(DB.class, JDBCTmpl.class);
  }


  @Test
  public void testExecTx() {
    AtomicInteger acc = new AtomicInteger(0);
    ExecutorService executorService =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
    List<CompletableFuture> futures = new ArrayList<>();

    long s = S.now();
    S._repeat(() -> {
      futures.add(CompletableFuture.runAsync(
          () -> {
            db.post(tmpl -> {
              for (int i = 0; i < 20; i++)
                tmpl.exec("INSERT INTO test values(?,?)",
                          String.valueOf(Math.random()), String.valueOf(acc.getAndIncrement()));
            });

          }, executorService));
    }, 10);

    try {
      CompletableFuture.allOf(S._for(futures).toArray(CompletableFuture[]::new)).thenRun(() -> {
        long beforeSelect = S.now();
        S._for((List<Record>) db.get(t -> t.query("SELECT * FROM test")));
        S.echo("Query time:" + (S.now() - beforeSelect));
        S.echo("ALL FINISHED : time usage " + (S.now() - s));
      }).get();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }


  }

  @After
  public void after() {
    db.post(tmpl -> tmpl.exec(
        "DROP TABLE IF EXISTS test;"
    ));
  }
}
