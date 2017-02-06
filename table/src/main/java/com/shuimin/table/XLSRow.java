package com.shuimin.table;

import java.util.ArrayList;

public class XLSRow extends ArrayList<Object> {
    int maxCol;

    public XLSRow(int maxCol){
        this.maxCol = maxCol;
    }

    @Override
    public Object get(int index) {
        if(index < maxCol)
            return super.get(index);
        return null;
    }
}