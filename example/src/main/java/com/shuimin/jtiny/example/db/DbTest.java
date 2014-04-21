package com.shuimin.jtiny.example.db;

import com.shuimin.jtiny.codec.connpool.ConnectionConfig;
import com.shuimin.jtiny.codec.connpool.ConnectionPool;
import com.shuimin.jtiny.codec.db.DB;
import com.shuimin.jtiny.codec.db.Record;
import com.shuimin.jtiny.core.Dispatcher;
import com.shuimin.jtiny.core.Server;
import com.shuimin.jtiny.core.misc.Renderable;
import com.shuimin.jtiny.core.mw.Action;
import com.shuimin.jtiny.core.mw.router.Router;

import java.io.InputStream;
import java.sql.SQLException;

import static com.shuimin.base.S.dump;
import static com.shuimin.jtiny.core.Interrupt.render;
import static com.shuimin.jtiny.core.misc.Renderable.text;

/**
 * Created by ed on 2014/4/17.
 */
public class DbTest {


    public static Server createServer(Dispatcher app) {
        return Server.basis(Server.BasicServer.jetty).use(app);
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

    public static void main(String[] args) {
        ConnectionPool pool = createPool();
        Dispatcher app = new Dispatcher(Router.regex());
        app.get("/db", Action.fly(() -> {
            DB.fire(pool.getConnection(), (tmpl) ->
                    tmpl.find("SELECT * FROM t_crm_delivery_detail limit 0 ,10"),
                (recordList) -> {
                    render(text(dump(recordList)));
                    return recordList;
                }
            );
        }));
        app.get("/blob", Action.fly(() -> {
            DB.fire(pool.getConnection(), (tmpl) ->
                    tmpl.find("SELECT * from t_attachment limit 0,1"),
                (recordList) -> {
                    Record r = recordList.get(0);
                    render(Renderable.stream((InputStream) r.get("attachment")));
                    return null;
                }
            );
        }));
        createServer(app).listen(8080);
    }


}
