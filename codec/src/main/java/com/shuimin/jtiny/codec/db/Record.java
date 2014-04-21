package com.shuimin.jtiny.codec.db;


import com.shuimin.base.util.logger.Logger;

import java.util.HashMap;

/**
 * <p>数据记录抽象</p>
 */
public class Record extends HashMap<String,Object> {

    public static Logger logger = Logger.get();

    public String tableName;

    public String tableName() {
        return tableName;
    }

    @Override
    public Record put(String key, Object val) {
        super.put(key,val);
        return this;
    }

}
