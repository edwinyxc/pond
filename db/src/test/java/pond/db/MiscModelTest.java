package pond.db;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import pond.db.connpool.SimplePool;

import javax.sql.DataSource;
import java.util.List;

import static pond.common.S._for;

/**
 * Created by ed on 10/31/14.
 */
public class MiscModelTest {

    DB db;
    DataSource dataSource;
    static String db_name = "POND_DB_TEST";

/*DROP DATABASE POND_DB_TEST;

CREATE DATABASE POND_DB_TEST;

USE POND_DB_TEST;

CREATE TABLE test (
    id varchar(64) primary key,
    `value` varchar(2000)
);

INSERT INTO test values('2333','233333');
INSERT INTO test values('2334','2333334');*/

    @Before
    public void init() {
        this.dataSource = new SimplePool().config(
                "com.mysql.jdbc.Driver",
                "jdbc:mysql://127.0.0.1:3306/",
                "root",
                "root" );

        this.db = new DB(dataSource);

        db.post( tmpl -> tmpl.tx(
                "DROP DATABASE IF EXISTS POND_DB_TEST;",
                "CREATE DATABASE POND_DB_TEST;",
                "USE POND_DB_TEST;",
                "CREATE TABLE test ( id varchar(64) primary key, `value` varchar(2000) );",
                "INSERT INTO test values('2333','233333');",
                "INSERT INTO test values('2334','2333334');"
        ));
    }

    @Ignore
    @Test
    public void testSql() {

        List<Record> rlist = this.db.get(t -> t.query("select `value` from test where id = '2333'"));

        String v = _for(rlist).first().get("value");

        Assert.assertEquals("233333",v);

    }

    @After
    public void after() {
        db.post( tmpl -> tmpl.tx(
                "DROP DATABASE POND_DB_TEST;"
        ));
    }

}
