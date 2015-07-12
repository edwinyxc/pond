package pond.db;

import pond.common.S;
import pond.common.f.Function;
import pond.common.f.Tuple;
import pond.db.connpool.SimplePool;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by ed on 1/28/15.
 */
public class PerformanceTest {

    //tested in our studio server
    public static DataSource localDataSource = SimplePool.Mysql()
            .host("localhost")
            .database("shuimin_map")
            .username("root")
            .password("root").build();

    public static void main(String[] args) {

        DB db = new DB(localDataSource);
        Function<Map, ResultSet> mapper = (rs -> new HashMap() {{
            try {
                this.put("title", rs.getString("title"));
                this.put("id", rs.getString("id"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }});

        for (int i = 0; i < 30; i++) {
            S.echo(S.time(() -> db.get(t -> t.query(mapper,
                    "SELECT * FROM t_mgmt GROUP BY title"))));
            S.echo(S.time(() -> db.get(t -> t.query("SELECT * FROM t_mgmt GROUP BY title"))));
        }


        //useNativeJdbc();
    }


    public static void useNativeJdbc() {
        try {
            long start = S.now();
            Connection conn = localDataSource.getConnection();
            long time_conn = S.now();
            ResultSet rs = conn.
                    prepareStatement("SELECT title, count(*)  percent FROM t_mgmt GROUP BY title")
                    .executeQuery();
            List r = new ArrayList<>();
            while (rs.next()) {
                Tuple<String, Integer> result = Tuple.t2(rs.getString("title"), rs.getInt("percent"));
                r.add(result);
            }
            long time_data_fetch = S.now();
            conn.close();
            S.echo(r);
            long time_conn_close = S.now();


            S.echo("conn_creation:" + String.valueOf(time_conn - start));
            S.echo("data_fetch:" + String.valueOf(time_data_fetch - time_conn));
            S.echo("close:" + String.valueOf(time_conn_close - time_data_fetch));
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
