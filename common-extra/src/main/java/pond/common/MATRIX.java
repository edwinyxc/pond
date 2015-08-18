package pond.common;

import pond.common.cui.Rect;
import pond.common.struc.Matrix;

public class MATRIX {


  public static Matrix console(int maxLength) {
    return new Matrix(0, maxLength);
  }

  /**
   * <p>
   * Print a matrix whose each row as a String.
   * </p>
   *
   * @return a string represent the input
   * matrix using '\n' to separate between lines
   */
  public static String mkStr(Rect r) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < r.height; i++) {
      for (int j = 0; j < r.width; j++) {
        char c = (char) r.data.get(i, j);
        sb.append(c);
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  public static Matrix addHorizontal(Matrix... some) {
    int height = 0;
    int width = 0;
    for (Matrix x : some) {
      height = S.math.max(height, x.rows());
      width += x.cols();
    }

    int[][] out = new int[height][width];
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        out[i][j] = ' ';
      }
    }

    int colfix = 0;
    for (Matrix x : some) {
      for (int h = 0; h < x.rows(); h++) {
        int[] row = x.row(h);
        for (int _i = 0; _i < row.length; _i++) {
          out[h][colfix + _i] = (char) row[_i];
        }
      }
      colfix += x.cols();
    }
    return new Matrix(out);
  }

  public static Matrix fromString(String... s) {
    final int maxLen = S.array(s).reduce(
        (String a, String b) -> {
          if (a == null || b == null) {
            return "";
          }
          return a.length() > b.length() ? a : b;
        }
    ).length();

    int[][] ret = new int[s.length][maxLen];
    for (int i = 0; i < s.length; i++) {
      for (int j = 0; j < s[i].length(); j++) {
        ret[i][j] = s[i].charAt(j);
      }
    }
    return new Matrix(ret);
  }
}
