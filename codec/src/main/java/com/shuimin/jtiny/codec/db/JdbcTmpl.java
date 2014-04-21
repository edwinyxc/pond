package com.shuimin.jtiny.codec.db;

import java.io.Closeable;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.shuimin.base.S._throw;
import static com.shuimin.jtiny.core.Server.G.debug;

/**
 * Created by ed on 2014/4/18.
 */
public class JdbcTmpl implements Closeable{

    RowMapper rm = (rs)-> {
        Record ret = new Record();
        ResultSetMetaData metaData = rs.getMetaData();
        int cnt = metaData.getColumnCount();
        for(int i = 0; i< cnt; i++) {
            String className = metaData.getColumnClassName(i+1);
            debug(className);
            Class<?> type = Object.class;
            try{
                type = Class.forName(className);
            } catch (ClassNotFoundException e) {
                debug(className);
            }

            String colName = metaData.getColumnName(i+1);
            Object val;
            if(type.equals(byte[].class)
                || type.equals(Byte[].class)) {
                val = rs.getBinaryStream(i+1);
            }
            else {
                val = rs.getObject(i+1,type);
            }
            ret.put(colName,val);
        }
        return ret;
    };

    public void txStart() {
        oper.transactionStart();
    }

    public void txEnd() {
        oper.transactionCommit();
    }

    JdbcOperator oper;

    public JdbcTmpl(JdbcOperator oper){
        this.oper = oper;
    }

    public List<Record> find(String sql) {
        ResultSet rs = oper.executeQuery(sql);
        List<Record> list = new ArrayList<>();
        try {
            while (rs.next()) {
               list.add(rm.map(rs));
            }
        }catch (SQLException e) {
           _throw(e);
        }
        return list;
    }

    public int exec(String sql) {
        return oper.executeUpdate(sql);
    }

    public ResultSet query(String sql) {
        return oper.executeQuery(sql);
    }

    public boolean add(Record r) {
        //TODO
        return false;
    }

    public boolean del(Record r) {
        //TODO
        return false;
    }

    public boolean upd(Record r) {
        //TODO
        return false;
    }

    @Override
    public void close() throws IOException {
        this.oper.close();
    }
}
