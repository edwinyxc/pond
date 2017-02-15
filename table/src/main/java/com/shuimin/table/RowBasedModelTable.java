package com.shuimin.table;

import pond.common.f.Function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by ed on 6/18/14.
 */
public abstract class RowBasedModelTable extends ModelTable {

  public <E> List<E> parse(int r_b, int r_e,
                           Function<E, XLSRow> row_mapper) {
    List<E> ret = new ArrayList<>();
    for (int i = r_b; i < r_e; i++) {
      XLSRow _row = this.row(i);
      E e = row_mapper.apply(_row);
      ret.add(e);
    }
    return ret;
  }


  public Function<Map<String, Object>, XLSRow> theadMapper(int idx) {

    List<Object> head = row(idx);

    return (list) -> {
      Map<String, Object> ret = new HashMap<>();
      for (int i = 0; i < head.size(); i++) {
        ret.put(String.valueOf(head.get(i)), list.get(i));
      }
      return ret;
    };
  }


}
