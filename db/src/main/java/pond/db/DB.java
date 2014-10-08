package pond.db;

import pond.common.S;
import pond.common.SPILoader;
import pond.common.abs.Makeable;
import pond.common.f.Callback;
import pond.common.f.Function;
import pond.db.spi.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.sql.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by ed on 2014/4/15.
 * <pre>
 *     面向Connection的数据库连接抽象
 *     try(DB b = new DB().....){
 *         //do your job
 *     }
 * </pre>
 */
public class DB implements Makeable<DB>, Closeable {

    static Logger logger = LoggerFactory.getLogger(DB.class);
    private JDBCTmpl tmpl;

    public DB() {
    }


    /**
     * 存放连接数据库的表结构(字段类型)
     */
    static Map<String, Map<String, Integer>> table_types;


    public static int[] getTypes(String table, String[] keys) {
        S._assert(table);
        S._assert(keys);
        int[] types = new int[keys.length];
        for (int i = 0; i < types.length; i++) {
            types[i] = DB.getType(table,keys[i]);
        }
        return types;
    }

    /**
     * Call once
     */
    static private Map<String, Map<String, Integer>> initType() {
        ResultSet rs_db = null;
        ResultSet rs_table = null;
        Map<String, Map<String, Integer>> table_types =
                new HashMap<>();
        Connection conn = null;
        try {
            conn = getConnFromPool();
            DatabaseMetaData meta = conn.getMetaData();
            rs_db = meta.getTables(null, "%", "%", new String[]{"TABLE"});
            while (rs_db.next()) {
                Map<String, Integer> table = new HashMap<>();
                String tablename = rs_db.getString("TABLE_NAME");
                table_types.put(tablename, table);
            }

            for (Map.Entry<String, Map<String, Integer>> e : table_types.entrySet()) {
                String table_name = e.getKey();
                Map<String, Integer> table = e.getValue();
                rs_table = meta.getColumns(null, null, table_name, null);
                while (rs_table.next()) {
                    String name = rs_table.getString("COLUMN_NAME");
                    Integer type = rs_table.getInt("DATA_TYPE");
                    table.put(name, type);
                }
            }

            //debug
            System.out.println("dump table_types:" + S.dump(table_types));
            return table_types;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs_db != null)
                    rs_db.close();
                if (rs_table != null)
                    rs_table.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static int getType(String table, String field) {
        if (table_types == null) {
            table_types = initType();
        }
        return table_types.getOrDefault(table, Collections.emptyMap()).get(field);
    }


    public static Connection getConnFromPool() {
        ConnectionPool connectionPool =
                SPILoader.service(ConnectionPool.class);
        return connectionPool.getConnection();
    }

    public static <R> R fire(Function<R, JDBCTmpl> process) {
        try (DB b = new DB().open(DB::getConnFromPool)) {
            return b.exec(process);
        }
    }

    public static void post(Callback<JDBCTmpl> cb){
        try(DB b = new DB().open(DB::getConnFromPool)){
            b.exec((t)->{
                cb.apply(t);
                return null;
            });
        }
    }

    /**
     * <pre>
     *     快速的执行一个db操作
     * </pre>
     *
     * @param connectionProvider 数据库连接
     * @param process            数据库链接打开之后执行的操作，参数为 JdbcTmpl
     * @return 执行后的结果
     * @see JDBCTmpl
     */
    public static <R> R fire(Function.F0<Connection> connectionProvider,
                             Function<R, JDBCTmpl> process) {
        try (DB b = new DB().open(connectionProvider)) {
            return b.exec(process);
        }
    }

    /**
     * @param connectionProvider 数据库链接提供者
     * @param process            处理JdbcTmpl
     * @param finisher           二次处理
     * @param <R>                最终结果类型
     * @param <M>                中间结果类型
     * @return 最终计算结果
     * @see pond.db.DB
     * #fire(java.sql.Connection, pond.common.f.Function, pond.common.f.Function)
     */
    public static <R, M> R fire(Function.F0<Connection> connectionProvider,
                                Function<M, JDBCTmpl> process,
                                Function<R, M> finisher) {

        try (DB b = new DB().open(connectionProvider)) {
            return b.exec(process, finisher);
        }

    }

    public static Connection newConnection(String driverClass,
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
            throw new RuntimeException(e);
        }
    }

    /**
     * <p>打开一个jdbc数据库链接</p>
     *
     * @param driverClass
     * @param connUrl
     * @param username
     * @param password
     * @return this
     */
    public DB open(String driverClass,
                   String connUrl,
                   String username,
                   String password) {

        tmpl = new JDBCTmpl(new JDBCOper(
                newConnection(driverClass, connUrl, username, password)));
        return this;
    }

    /**
     * <p>得到当前DB对象的jdbcTmpl值</p>
     *
     * @return JdbcTmpl
     */
    public JDBCTmpl tmpl() {
        return tmpl;
    }

    /**
     * <p>做一次查询,并映射到结果集，然后返回结果集</p>
     *
     * @param queryFunc 查询方法
     * @param mapper    映射方法
     * @param <T>       返回类型
     * @return 类型为 T 的返回结果
     */
    public <T> T query(Function<ResultSet, JDBCTmpl> queryFunc,
                       Function<T, ResultSet> mapper) {
        return mapper.apply(queryFunc.apply(tmpl));
    }

    /**
     * <p>执行</p>
     *
     * @param process
     * @param <R>
     * @return
     */
    public <R> R exec(Function<R, JDBCTmpl> process) {
        return process.apply(tmpl);
    }

    /**
     * @see pond.db.DB#exec(pond.common.f.Function)
     */
    public <R, T> R exec(Function<T, JDBCTmpl> process,
                         Function<R, T> finisher) {
        return finisher.apply(process.apply(tmpl));
    }

    public DB open(Function.F0<Connection> connectionSupplier) {
        tmpl = new JDBCTmpl(new JDBCOper(connectionSupplier.apply()));
        return this;
    }

    @Override
    public void close() {
        try {
            if (tmpl != null) {
                tmpl.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            S._lazyThrow(e);
        }
    }
//
}
