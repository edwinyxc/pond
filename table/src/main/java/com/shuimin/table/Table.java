package com.shuimin.table;

import java.io.IOException;
import java.util.List;
import com.shuimin.table.*;

/**
 * Created by ed on 2014/5/4.
 * Table abstraction
 */
public interface Table {

  /**
   * initialize table
   *
   * @param i       row index
   * @param j       col index
   * @param initVal initValue
   * @return this
   * @throws java.io.IOException
   */
  public Table init(int i, int j, Object initVal) throws IOException;

  /**
   * get columns count
   *
   * @return count
   */
  //TODO try to optimize to O(1)
  public int cols();

  /**
   * get rows count
   *
   * @return count
   */
  //TODO try to optimize to O(1)
  public int rows();

  /**
   * get row as List at index of i
   *
   * @param i row index
   * @return List of row
   */
  public XLSRow row(int i);

  /**
   * get column as List at index of i
   *
   * @param i col index
   * @return list of column
   */
  public List<Object> col(int i);

  /**
   * get value at index of (i,j)
   *
   * @param i row index
   * @param j col index
   * @return value
   */
  public Object get(int i, int j);

  /**
   * set value at index of (i,j)
   *
   * @param i   row index
   * @param j   col index
   * @param val value
   */
  public void set(int i, int j, Object val);

  /**
   * Returns a 2D array of this table.
   * <pre>
   *     [[row1...],[row2...]]
   * </pre>
   *
   * @return 2D array represented this table
   */
  Object[][] toArray();
}
