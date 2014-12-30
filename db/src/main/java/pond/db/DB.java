package pond.db;

import pond.common.S;
import pond.common.f.Callback;
import pond.common.f.Function;
import pond.db.connpool.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pond.db.connpool.SimplePool;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
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
        ConnectionPool cp  = new SimplePool();
        cp.loadConfig(config);
        return cp;
    }

    public static <E extends Record> RecordService<E> dao(Class<E> cls){
        return Proto.dao(cls);
    }

    private DataSource dataSource;
    private F0<Connection> connProvider;


     /**
     * 存放连接数据库的表结构(字段类型)
     */
    Map<String, Map<String, Integer>> dbStruc;



    public DB(DataSource dataSource) {
        this.dataSource = dataSource;
        this.connProvider = () -> _try( () -> this.dataSource.getConnection() );
        this.dbStruc = initType();
    }


    /**
     * Call once
     */
    private Map<String, Map<String, Integer>> initType() {
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
     *
     * @param process
     * @param <R>
     * @return
     */
    public <R> R get(Function<R, JDBCTmpl> process) {
        try (JDBCTmpl tmpl = this.open()) {
            return process.apply(tmpl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void post(Callback<JDBCTmpl>... cbs){
        try(JDBCTmpl tmpl = this.open()){
             _for(cbs).each(cb -> cb.apply(tmpl));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a tmpl
     * @return
     */
    public JDBCTmpl open() {
        return new JDBCTmpl(new JDBCOper(connProvider.apply()),this.initType());
    }


//
}
