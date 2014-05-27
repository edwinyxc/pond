package com.shuimin.szgwyw;

import com.shuimin.common.S;
import com.shuimin.common.abs.Config;
import com.shuimin.pond.codec.connpool.ConnectionConfig;
import com.shuimin.pond.codec.connpool.ConnectionPool;
import com.shuimin.pond.codec.db.DB;
import com.shuimin.pond.codec.restful.Resource;
import com.shuimin.pond.core.Global;
import com.shuimin.pond.core.Pond;
import com.shuimin.pond.core.mw.Dispatcher;
import com.shuimin.pond.core.mw.StaticFileServer;
import com.shuimin.pond.core.spi.Router;
import com.shuimin.szgwyw.article.Articles;

import java.sql.SQLException;

public class App {
    public static void main(String[] args) {


        Dispatcher dispatcher = new Dispatcher();
        //list
        //
//        dispatcher.make(new Index());
        //
        //[IDEAL]
        //dispatcher.make([Module])
        dispatcher.use(Articles.ARTICLE_RES);
        dispatcher.use(Articles.BRIEF_RES);

        Config<Pond> connectionPoolConfig =
                ctx -> {
                    ConnectionConfig config = new ConnectionConfig(
                            "10", "com.mysql.jdbc.Driver", "root", "root",
                            "jdbc:mysql://127.0.0.1:3306/szgwyw?useUnicode=true&charactorEncoding=UTF-8"
                    );
                    ConnectionPool pool = new ConnectionPool();
                    try {
                        pool.init(config);
                    } catch (SQLException e) {
                        e.printStackTrace();//Ohh
                        System.exit(-1);
                    }
                    ctx.attr(DB.CONNECTION_POOL,pool);
                };

        Config<Pond> commonConfig =
                ctx -> ctx.attr(Global.TEMPLATE_PATH, "view");

        Pond.init(connectionPoolConfig, commonConfig)
                .debug()
                .use(dispatcher)
                .use(new StaticFileServer("www")).start(8080);
        S.echo(Pond.config(Global.ROOT));
    }
}
