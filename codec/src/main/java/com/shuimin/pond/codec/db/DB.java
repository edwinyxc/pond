package com.shuimin.pond.codec.db;

import com.shuimin.common.f.Function;
import com.shuimin.common.util.logger.Logger;
import com.shuimin.pond.codec.connpool.ConnectionPool;
import com.shuimin.pond.core.exception.UnexpectedException;
import com.shuimin.pond.core.misc.Makeable;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.shuimin.common.S._throw;

/**
 * Created by ed on 2014/4/15.
 * <p>数据库抽象，使用自定义名称（逻辑名称）来区分，
 * 每一个db之间可以是同一个物理实现（例如在mysql中，就是同一个 database）
 * ，也可以是多个。具体采用连接池还是单个连接（on the fly）取决于构造，
 * 单个DB对象持有一个打开的connection，显示调用的关闭不一定会真的释放connection</p>
 */
public class DB implements Makeable<DB>, Closeable {
    private static final Logger logger = Logger.get();
    private JdbcTmpl tmpl;

    public DB() {
    }

    public static <R> R fire(Connection connection,
                           Function<R,JdbcTmpl> process) {
        try(DB b = new DB().open(()->connection)) {
            return  b.exec(process);
        }
    }

    public static <R, M> R fire(Connection connection,
                                Function<M, JdbcTmpl> process,
                                Function<R, M> finisher) {

        try (DB b = new DB().open(() -> connection)) {
            return b.exec(process, finisher);
        }

    }

    public DB open(String driverClass,
                   String connUrl,
                   String username,
                   String password) {

        tmpl = new JdbcTmpl(new JdbcOperator(
            newConnection(driverClass, connUrl, username, password)));
        return this;
    }

    public JdbcTmpl tmpl() {
        return tmpl;
    }


    public <T> T query(Function<ResultSet, JdbcTmpl> queryFunc,
                       Function<T, ResultSet> mapper) {
        return mapper.apply(queryFunc.apply(tmpl));
    }

    public <R> R exec(Function<R,JdbcTmpl> process) {
       return process.apply(tmpl);
    }

    public <R, T> R exec(Function<T, JdbcTmpl> process,
                         Function<R, T> finisher) {
        return finisher.apply(process.apply(tmpl));
    }

    public DB open(Function._0<Connection> connectionSupplier) {
        tmpl = new JdbcTmpl(new JdbcOperator(connectionSupplier.apply()));
        return this;
    }

    public DB open(ConnectionPool p) {
        tmpl = new JdbcTmpl(new JdbcOperator(p.getConnection()));
        return this;
    }

    protected static Connection newConnection(String driverClass,
                                              String connUrl,
                                              String username,
                                              String password) {
        try {
            DriverManager.registerDriver((java.sql.Driver)
                Class.forName(driverClass).newInstance());
        } catch (ClassNotFoundException e) {
            logger.debug("cont find class [" + driverClass + "]");
            e.printStackTrace();
        } catch (SQLException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        try {
            return DriverManager.getConnection(connUrl, username, password);
        } catch (SQLException e) {
            logger.debug("cont open connection, sql error: " + e.toString());
            throw new UnexpectedException(e);
        }
    }

    @Override
    public void close() {
        try {
            if (tmpl != null) {
                tmpl.close();
            }
        } catch (IOException e) {
            _throw(e);
        }
    }

}
