package com.shuimin.pond.example.db;

import com.shuimin.common.S;
import com.shuimin.common.f.Function;
import com.shuimin.pond.codec.connpool.ConnectionConfig;
import com.shuimin.pond.codec.connpool.ConnectionPool;
import com.shuimin.pond.codec.db.AbstractRecord;
import com.shuimin.pond.codec.db.DB;
import com.shuimin.pond.codec.db.Record;
import com.shuimin.pond.core.Pond;
import com.shuimin.pond.core.Renderable;
import com.shuimin.pond.core.mw.Action;
import com.shuimin.pond.core.mw.Dispatcher;
import com.shuimin.pond.core.spi.Router;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static com.shuimin.common.S.dump;
import static com.shuimin.common.S.echo;
import static com.shuimin.pond.core.Interrupt.render;
import static com.shuimin.pond.core.Renderable.text;

/**
 * Created by ed on 2014/4/17.
 */
public class DbTest {


    public static Pond createServer(Dispatcher app) {
        return Pond.init().use(app);
    }

    public static ConnectionPool createPool() {
        ConnectionPool pool = new ConnectionPool();
        try {
            pool.init(new ConnectionConfig(
                "10", "com.mysql.jdbc.Driver", "root", "root",
                "jdbc:mysql://192.168.0.88:3306/bi"
            ));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pool;
    }

    final static ConnectionConfig local = new ConnectionConfig("10",
        "com.mysql.jdbc.Driver", "root", "root",
        "jdbc:mysql://localhost:3306/test"
    );

    public static Connection createLocal() {

        return DB.newConnection(local);
    }

    public static ConnectionPool createLocalPool() {
        ConnectionPool pool = new ConnectionPool();
        try {
            pool.init(local);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pool;
    }

    public static void main(String[] args) {
        ConnectionPool pool = createPool();
        ConnectionPool localPool = createLocalPool();
        Dispatcher app = new Dispatcher();
        app.get("/db", Action.fly(() ->
                render(text(dump(DB.fire(pool::getConnection, tmpl ->
                        tmpl.find("SELECT * FROM t_crm_delivery_detail limit 0 ,10")
                ))))
        ));
        app.get("/blob", Action.fly(() -> {
            DB.fire(pool::getConnection, (tmpl) -> {
                    render(Renderable.stream((InputStream)
                            tmpl.find("SELECT * FROM t_attachment limit 0,1")
                                .get(0).get("attachment")
                    ));
                    return null;
                }
            );
        }));
        app.get("/join", Action.fly(() -> {
            Record rr =
                DB.fire(pool::getConnection, tmpl -> {
                    String sql = "SELECT * FROM user u LEFT JOIN user_group_relation r" +
                        " ON u.id = r.uid LIMIT 0,1";
                    return tmpl.find(sql).get(0);
                });
            render(text(dump(rr)));
        }));

        app.get("/type", Action.fly(() -> {
                ConnectionConfig config = new ConnectionConfig(
                    "10", "com.mysql.jdbc.Driver", "root", "root",
                    "jdbc:mysql://192.168.0.88:3306/14_s6"
                );
                Connection conn = DB.newConnection(config);
                render(text(dump(DB.fire(() -> conn, tmpl ->
                    tmpl.find("SELECT * FROM test")).get(0))));
            }
        ));

        String create0 = "DROP TABLE IF EXISTS db_test";
        String create1 =
            " CREATE TABLE db_test (" +
                "id varchar(64) primary key," +
                "value varchar(55) ," +
                "test text" +
                ")";

        app.get("/create", Action.fly(() -> {
                DB.fire(DbTest::createLocal, tmpl ->
                    tmpl.tx(create0, create1));
                render(text("created!"));
            }
        ));


        app.get("/insert", Action.fly(() -> {
            Record r = new AbstractRecord() {
            };

            r.table("db_test");
            r.set("id", "45_id");
            r.set("value", "45_value");
            r.set("test", "sdddddweqwecvz");

            DB.fire(DbTest::createLocal, tmpl ->
                    tmpl.add(r)
            );

            render(text(dump(DB.fire(DbTest::createLocal,
                tmpl -> tmpl.find("SELECT * FROM db_test where id = '45_id'")).get(0))));

        }));

        final Function.F0<Record> findR = () -> {
            List<Record> rList = DB.fire(localPool::getConnection, tmpl ->
                    tmpl.<Record>find("SELECT * FROM db_test where id = '45_id'")
            );
            if(rList.size() == 0) return null;
                Record r = rList.get(0);
            return r;
        };

        app.get("/update", Action.fly(() ->
            {
                Record r = findR.apply();
                echo(r);
                int val = S.parse.toUnsigned((String) r.get("value"));
                r.set("value", String.valueOf(val +1));
                r.PK("id");
                DB.fire(localPool::getConnection, tmpl ->
                        tmpl.upd(r)
                );
                render(text(dump(findR.apply())));
            }
        ));


        app.get("/del",Action.fly(()-> {
            Record r = findR.apply();
            echo(r);
            DB.fire(localPool::getConnection, tmpl->
                tmpl.del(r)
            );
            render(text(dump(findR.apply())));
        }));


        createServer(app).debug().start(8080);
    }


}
