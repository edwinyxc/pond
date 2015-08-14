package pond.common.cui;

import pond.common.S;
import pond.common.f.Function;
import pond.common.struc.Matrix;

public class Rect {

  public final int width;
  public final int height;
  public Matrix data;

  public Rect(Matrix data) {
    this.width = data.cols();
    this.height = data.rows();
    this.data = data;
  }

  public Rect(final String[] str) {
    this(str, 0, 0);
  }

  public Rect(String str, String s) {
    this(str.split(s));
  }

  public Rect(final String[] str, int width, int height) {
    this.width = S.math.max(((Function.F0<Integer>) () -> {
      int maxLen = 0;
      for (String s : str) {
        s = s.replaceAll("\n", "");
        maxLen = s.length() > maxLen ? s.length() : maxLen;
      }
      return maxLen;
    }).apply(), width);
    this.height = S.math.max(str.length, height);
    int[][] tmp = new int[this.height][this.width];
    for (int h = 0; h < this.height; h++) {
      for (int w = 0; w < this.width; w++) {
        if (h < str.length && w < str[h].length()) {
          tmp[h][w] = str[h].charAt(w);
        } else {
          tmp[h][w] = ' ';
        }

      }
    }
    data = new Matrix(tmp);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("");
    for (int i = 0; i < data.rows(); i++) {
      for (int j = 0; j < data.cols(); j++) {
        sb.append((char) data.get(i, j));
      }
      sb.append("\n");
    }
    return sb.toString();
  }
}
