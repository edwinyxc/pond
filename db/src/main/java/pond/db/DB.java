package pond.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.S;
import pond.common.f.Callback;
import pond.common.f.Function;
import pond.db.connpool.ConnectionPool;
import pond.db.connpool.SimplePool;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static pond.common.S._for;
import static pond.common.S._try;
import static pond.common.f.Function.F0;


/**
 * Created by ed on 2014/4/15.
 * <pre>
 *     面向Connection的数据库连接抽象
 *     try(DB b = new DB().....){
 *         //do your job
 *     }
 * </pre>
 */
public final class DB {

    static Logger logger = LoggerFactory.getLogger(DB.class);

    public static ConnectionPool SimplePool(Properties config) {
        ConnectionPool cp = new SimplePool();
        cp.loadConfig(config);
        return cp;
    }

    public static <E extends Record> RecordService<E> dao(Class<E> cls) {
        return Proto.dao(cls);
    }

    private DataSource dataSource;
    private F0<Connection> connProvider;
    private JDBCTmpl tmpl;
    private JDBCOper oper;

    private MappingRule rule;

    /**
     * database structures (types as int)
     */
    Map<String, Map<String, Integer>> dbStructures;


    public DB(DataSource dataSource) {
        this.dataSource = dataSource;
        this.connProvider = () -> _try((Function.F0ERR<Connection>)
                this.dataSource::getConnection);
        rule = new MappingRule();
        this.dbStructures = getDatabaseStructures();
        tmpl = new JDBCTmpl(this.dbStructures,rule);
        oper = new JDBCOper();
    }

    public DB rule(MappingRule rule) {
        this.rule = rule;
        return this;
    }


    /**
     * This function will be only called ONCE
     */
    private Map<String, Map<String, Integer>> getDatabaseStructures() {
        ResultSet rs_db = null;
        ResultSet rs_table = null;
        Map<String, Map<String, Integer>> table_types =
                new HashMap<>();
        Connection conn = null;
        try {
            conn = connProvider.apply();
            DatabaseMetaData meta = conn.getMetaData();
            rs_db = meta.getTables(null, "%", "%", new String[]{"TABLE"});
            while (rs_db.next()) {
                String tablename = rs_db.getString("TABLE_NAME");
                table_types.put(tablename, new HashMap<>());
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
//            System.out.println("dump table_types:" + S.dump(table_types));
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


    /**
     * @param process -- the 'get' process
     * @param <R>     -- the Record type
     * @return Record or Anything mapped
     */
    public <R> R get(Function<R, JDBCTmpl> process) {
        long startTime = S.time();
        try (JDBCTmpl tmpl = this.open()) {
            logger.debug("open db time used: " + (S.time() - startTime));
            R r = process.apply(tmpl);
            logger.debug("apply process time used: " + (S.time() - startTime));
            return r;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<? extends Record> get(String sql, Object... args) {
        return get(t -> t.query(sql, args));
    }

    /* going to delete in future
    @Deprecated
    public <R> R _get_failback(Function<R, JDBCTmpl> process) {
        long startTime = S.time();
        JDBCTmpl tmpl = null;
        try {
            tmpl = this.open();
            logger.debug("open db time used: " + (S.time() - startTime));
            R r = process.apply(tmpl);
            logger.debug("apply process time used: " + (S.time() - startTime));
            return r;
        } finally {
            try {
                if (tmpl != null) tmpl.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    */

    public void post(Callback<JDBCTmpl> cb) {
        try (JDBCTmpl tmpl = this.open()) {
            cb.apply(tmpl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void post(String sql, Object... args) {
        post(t -> t.exec(sql, args));
    }

    /**
     * Returns a tmpl
     *
     * @return
     */
    public JDBCTmpl open() {
        return tmpl.open(oper.open(connProvider.apply()));
    }


}
