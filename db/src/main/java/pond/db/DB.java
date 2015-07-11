package pond.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.common.S;
import pond.common.f.Callback;
import pond.common.f.Function;
import pond.common.f.Tuple;
import pond.db.connpool.ConnectionPool;
import pond.db.connpool.SimplePool;
import pond.db.sql.dialect.Dialect;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.*;

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

    public static Logger logger = LoggerFactory.getLogger(DB.class);

    public static ConnectionPool SimplePool(Properties config) {
        ConnectionPool cp = new SimplePool();
        cp.loadConfig(config);
        return cp;
    }

    public static <E extends Record> RecordService<E> dao(Class<E> cls) {
        return Proto.dao(cls);
    }

    private DataSource dataSource;

    F0<Connection> connProvider;
    MappingRule rule;
    Dialect dialect;
    /**
     * * default query
     * TODO: ugly implement
     */
    final Function<AbstractRecord, ResultSet> _default_rm =

            (ResultSet rs) -> {
                AbstractRecord ret = Record.newValue(AbstractRecord.class);
                try {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int cnt = metaData.getColumnCount();

                    String field_name;
                    int field_type;
                    int field_idx;
                    for (int i = 0; i < cnt; i++) {
                        field_idx = i + 1;
                        field_name = metaData.getColumnName(field_idx);
                        field_type = metaData.getColumnType(field_idx);

                        S._assert(ret);
                        ret.set(field_name, rule.getMethod(field_type).apply(rs, field_name));
                    }

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                return ret;
            };

    /**
     * database structures (types as int)
     */
    Map<String, Map<String, Integer>> dbStructures;

    public DB(DataSource dataSource, Dialect dialect) {
        this.dataSource = dataSource;
        this.connProvider = () -> S._try_ret(this.dataSource::getConnection);
        rule = new MappingRule();
        this.dbStructures = getDatabaseStructures();
    }

    public DB(DataSource dataSource) {
        //using mysql dialect as default and do not seek reason
        this(dataSource, Dialect.mysql);
    }

    /**
     * for the purpose of reconfigure the mapping rule
     */
    public DB rule(MappingRule rule) {
        this.rule = rule;
        return this;
    }

    /**
     * Returns a tmpl
     */
    JDBCTmpl open() throws SQLException {
        long s = S.now();
        Connection connection = connProvider.apply();
        S.echo("get connection time usage: " + (S.now() - s));
        JDBCTmpl tmp = new JDBCTmpl(this, connection);
        S.echo("build tmpl: " + (S.now() - s));
        return tmp;
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
        long startTime = S.now();
        R r;
        try (JDBCTmpl tmpl = open()) {
            S._debug(logger, log -> log.debug(("newTmpl db time used: " + (S.now() - startTime))));
            tmpl.txStart();
            r = process.apply(tmpl);
            tmpl.txCommit();
            S._debug(logger, log -> log.debug("apply process time used: " + (S.now() - startTime)));
            return r;
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    public List<Record> get(String sql, Object... args) {
        return get(t -> t.query(sql, args));
    }


    public void post(Callback<JDBCTmpl> cb) {
        try (JDBCTmpl tmpl = open()) {
            tmpl.txStart();
            cb.apply(tmpl);
            tmpl.txCommit();
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * post all arguments as sql
     */
    public void batch(String... posts) {

        try (JDBCTmpl tmpl = open()) {
            tmpl.txStart();
            S._for(posts).each(tmpl::exec);
            tmpl.txCommit();
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Quick post with args
     */
    public void post(String sql, Object... args) {
        post(t -> t.exec(sql, args));
    }


}
