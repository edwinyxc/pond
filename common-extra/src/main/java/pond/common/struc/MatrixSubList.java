package pond.common.struc;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ed
 */
@SuppressWarnings("rawtypes")
public class MatrixSubList {

  public final Matrix root_m;//group of sublist share the same one
  private final List root;
  private int row; //index of current sublist

  public MatrixSubList(List src) {
    this.root = src;
    this.root_m = new Matrix(1, 2);
    root_m.set(0, 0, 0);
    root_m.set(0, 1, src.size());
    row = 0;
  }

  private MatrixSubList(MatrixSubList m, int b, int e)
      throws InvalidParameterException {
    if (e < b || e > m.getSize() || b < 0) {
            /*err*/
      throw new InvalidParameterException(
          "!!!assert failure!!!\n"
              + "!!!b=" + b + ";e=" + e
              + "!!!m.size=" + m.getSize() + "\n"
              + "root_m=" + m.root.toString() + "\n"
      );
    }
    this.root = m.root;
    this.root_m = m.root_m;

    int[] mAp = m.getAp();

    int mb = mAp[0];

    int[] shadow = {mb + b, mb + e};
    root_m.addRow(shadow);
    /*last row is the limit row*/
    this.row = root_m.rows() - 1;

//		X.echo("row="+row);
  }

  public int getSize() {
    int[] ap = root_m.row(this.row);
    return ap[1] - ap[0];
  }

  private int[] getAp() {
    return root_m.row(row);
  }

  @SuppressWarnings("unchecked")
  private List reified(int[] arr) {
    List ret = new ArrayList();
    for (int i = arr[0]; i < arr[1]; i++) {
      ret.add(this.root.get(i));
    }
    return ret;
  }

  /*API -------*/
  public List list() {
    return reified(
        getAp());
  }

  public List getList(int row) {
    if (row > 0 && row < root_m.rows()) {
      return reified(
          root_m.row(row));
    }
    return null;
  }

  public List root() {
    return root;
  }

  public MatrixSubList sub(int b, int e)
      throws InvalidParameterException {
    return new MatrixSubList(this, b, e);
  }

  public void add(Object o) {
    _addHelper(getSize(), o);
  }

  public void add(int idx, Object o) {
    _addHelper(idx, o);
  }

  public void addAll(List objs) {
    _addListHelper(getSize(), objs);
  }

  public void addAll(int idx, List objs) {
    _addListHelper(idx, objs);
  }

  public void del(int index) {
    _delHelper(index, 1);
  }

  public void del(int b, int e) {
    _delHelper(b, e - b);
  }

  public void clear() {
    _delAllHelper();
  }

  /**
   * @param idx relative index of the subList
   * @param obj Object to add
   */
  @SuppressWarnings("unchecked")
  private void _addHelper(int idx, Object obj) {
    int[] ap = getAp();
    int ab = ap[0] + idx;
    root.add(ab, obj);
    apMod(ab, 1);
  }

  @SuppressWarnings("unchecked")
  private void _addListHelper(int idx, List objs) {
    int[] ap = getAp();
    int ab = ap[0] + idx;
    for (int i = 0; i < objs.size(); i++) {
      root.add(ab + i, objs.get(i));
      apMod(ab, 1);
    }
  }

  private void _delHelper(int idx, int length) {
    int[] ap = getAp();
    int ab = ap[0] + idx;
    for (int i = 0; i < length; i++) {
      root.remove(ab);
      apMod(ab, -1);
    }
  }

  /**
   * delete the entire row of the matrix and the affected (after) remain
   * same row index , the deleted row are just all in shadow_off state
   */
  private void _delAllHelper() {
    _delHelper(0, getSize());
  }

  /**
   * any modify works to the right side of the index of the cur parameter
   * indicates
   *
   * @param cur current position of modified element index (absolute)
   * @param mod move amount -Integer can be also neg or pos
   */
  private void apMod(int cur, int mod) {
    int[] ap;
    for (int r = 0; r < root_m.rows(); r++) {
      ap = root_m.row(r);
      /*assert ap[0] < ap[1]*/
      if (cur < ap[0]) {
        root_m.set(r, 0, ap[0] + mod);
        root_m.set(r, 1, ap[1] + mod);
      } else if (cur < ap[1] && cur >= ap[0]) {
        root_m.set(r, 1, ap[1] + mod);
      } else if (cur == ap[1] && mod > 0) {
        root_m.set(r, 1, ap[1] + mod);
      }
    }
  }
}
