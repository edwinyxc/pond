package pond.db;

import pond.common.S;
import pond.db.connpool.SimplePool;

import javax.sql.DataSource;


/**
 * Created by ed on 1/28/15.
 */
public class FastTest {

    public static void main(String[] args) {
        DataSource dataSource = new SimplePool().config(
                "com.mysql.jdbc.Driver",
                "jdbc:mysql://192.168.0.88:3306/shuimin_map",
                "root",
                "root");

        DB db = new DB(dataSource);
        S.echo(db.get(t -> t.query("SELECT title,count(*) percent FROM t_mgmt GROUP BY title")));
    }
}
