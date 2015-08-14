package com.shuimin.table;

import pond.common.f.Tuple;

import java.util.HashMap;
import java.util.Map;

import static pond.common.S._for;
import static pond.common.f.Tuple.t3;


/**
 * Created by ed on 6/3/14.
 */
public abstract class ModelTable implements Table {

  /**
   * Call the engine to resolve specified cell(i,j) and return
   * a Tuple with key and value if cell is an
   * expression else return null;
   *
   * @param i     row index
   * @param j     col index
   * @param value value
   * @return result of the expression or null if cell is not
   * a expression
   */
  protected abstract Tuple<String, Object> resolve(int i, int j, String value);

  public Map<String, Object> model() {
    return _for(model).map(t -> t._c).val();
  }

  public Map<String, Tuple.T3<Integer, Integer, Object>> fullModel() {
    return model;
  }

  /**
   * Get the structure of this ModelTable
   *
   * @return structure
   */
  public Map<String, Tuple.T3<Integer, Integer, Object>> analyze() {
    Map<String, Tuple.T3<Integer, Integer, Object>> ret =
        new HashMap<>();
    for (int i = 0, len = this.rows();
         i < len; i++) {
      for (int j = 0, clen = this.cols(); j < clen; j++) {
        Tuple<String, Object> parsed =
            resolve(i, j, String.valueOf(this.get(i, j)));
        if (parsed != null) {
          ret.put(parsed._a, t3(i, j, parsed._b));
        }
      }
    }
    model.clear();
    model.putAll(ret);
    return ret;
  }

  /**
   * Initialized once, stored structure of table and where expressions locates.
   */
  protected final Map<String, Tuple.T3<Integer, Integer, Object>> model
      = new HashMap<>();

  /**
   * Set model to the table.
   * Attention, no actual value will be set until
   * ${saveModel} been called.
   *
   * @param model
   */
  public void model(Map<String, Object> model) {
    _for(model).each(e -> {
      String key = e.getKey();
      Tuple.T3<Integer, Integer, Object> t = this.model.get(key);
      if (t != null)
        this.model.put(key, t3(t._a, t._b, e.getValue()));
    });
    // can be improved.
    saveModel();
  }

  private void saveModel() {
    _for(this.model).each(e -> {
      Tuple.T3<Integer, Integer, Object> t = e.getValue();
      //save to the 'real' table
      this.set(t._a, t._b, t._c);
    });
  }


}
