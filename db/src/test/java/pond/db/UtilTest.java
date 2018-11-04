package pond.db;

import org.junit.Ignore;
import org.junit.Test;
import pond.common.FILE;
import pond.common.PATH;
import pond.common.S;
import pond.common.STREAM;
import pond.common.f.Function;
import pond.common.f.Tuple;
import pond.db.connpool.ConnectionPool;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.sql.SQLType;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class UtilTest {

    DB db;

    @Test
    public void testA() {
        String a = String.format("jdbc:mysql://%s/%s?%s", "ssss", "weee", "wwww");
        assertEquals("jdbc:mysql://ssss/weee?wwww", a);
    }

    @Ignore
    public void test_DbStruc() {
        this.db = new DB(ConnectionPool.c3p0(new Properties() {
            {
                this.put(ConnectionPool.URL, "jdbc:mysql://192.168.3.55:3306/shuimin_wms?useUnicode=true&characterEncoding=UTF-8");
                this.put(ConnectionPool.DRIVER, "com.mysql.jdbc.Driver");
                this.put(ConnectionPool.USERNAME, "root");
                this.put(ConnectionPool.PASSWORD, "root");
                this.put(ConnectionPool.MAXSIZE, "30");
            }
        }));

        Map<String, Map<String, Integer>> struc = db.getDbStructures();
        S.echo("com.dota.Dota2".lastIndexOf("."));
        String modelName = "com.dota.Dota2";
        String rootPath = PATH.detectWebRootPath()+"/src/main/java/";
        Record.createTemplateFile(db, rootPath, "com.dota.Dota2", "t_sales_order");
    }

    @Test
    public void record_map_test() {

        Model a = new Model() {{
            id("id");
            field("f1");
        }};

        a.set("id", "123").set("f1", "15");


        Model b = new Model() {{
            id("id");
            field("f1");
        }};

        b.set("id", "333").set("f1", "16");

        a.mergeExceptId(b.toMap());
        assertEquals(a.id(), "123");
    }

    @Test
    public void test() {
        Object a = Collections.emptyMap().get("ss");
        System.out.print(a);
        S.echo(5000 * 3 + 8000 * 2 + 6000 + 12000);
        S.echo(5000 * 2 + 8000 * 4 + 12000);
    }
}
