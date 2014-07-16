package com.shuimin.pond.db;

import com.shuimin.common.S;
import com.shuimin.common.sql.Criterion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import static com.shuimin.common.S._for;

public class AbstractRecordTest {
    private Connection createConnection() {
        Connection connection;
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
//            connection = DriverManager.getConnection("jdbc:mysql://192.168.0.88:3306/bi", "root", "root");
            connection = DriverManager.getConnection("jdbc:mysql://10.10.10.104:3306/bi", "root", "root");
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void test1() {
        List<TestRecord> list =
                DB.fire(this::createConnection, t ->
                                t.map(TestRecord.class, "select * from t_crm_order")
                );
        S.echo(S.dump(_for(list).map(TestRecord::view).toList()));
    }

    public static void main(String[] arg) {
        new AbstractRecordTest().test1();
    }

}